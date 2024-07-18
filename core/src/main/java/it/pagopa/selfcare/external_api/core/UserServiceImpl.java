package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.external_api.api.MsCoreConnector;
import it.pagopa.selfcare.external_api.api.UserMsConnector;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.onboarding.Billing;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionInfo;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionResource;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionResponse;
import it.pagopa.selfcare.external_api.model.onboarding.mapper.OnboardingInstitutionMapper;
import it.pagopa.selfcare.external_api.model.user.*;
import it.pagopa.selfcare.onboarding.common.PartyRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Stream;

import static it.pagopa.selfcare.external_api.model.product.ProductOnboardingStatus.ACTIVE;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final MsCoreConnector msCoreConnector;
    private final UserMsConnector userMsConnector;
    private final OnboardingInstitutionMapper onboardingInstitutionMapper;

    @Autowired
    public UserServiceImpl(MsCoreConnector msCoreConnector, UserMsConnector userMsConnector, OnboardingInstitutionMapper onboardingInstitutionMapper) {
        this.msCoreConnector = msCoreConnector;
        this.userMsConnector = userMsConnector;
        this.onboardingInstitutionMapper = onboardingInstitutionMapper;
    }

    @Override
    public UserInfoWrapper getUserInfoV2(String fiscalCode, List<RelationshipState> userStatuses) {
        log.trace("geUserInfo start");
        final User user = userMsConnector.searchUserByExternalId(fiscalCode);
        List<OnboardedInstitutionInfo> onboardedInstitutions = getOnboardedInstitutionsDetails(user.getId(), null);
        List<String> userStatusesString = userStatuses == null ? Collections.emptyList()
                : userStatuses.stream().map(RelationshipState::toString).toList();

        List<OnboardedInstitutionResponse> onboardedInstitutionResponses =
                onboardedInstitutions.stream()
                        .filter(institution -> userStatusesString.isEmpty() ||
                                (Objects.nonNull(institution.getProductInfo()) && userStatusesString.contains(institution.getProductInfo().getStatus())))
                        .map(onboardedInstitution -> {
                            OnboardedInstitutionResponse response = onboardingInstitutionMapper.toOnboardedInstitutionResponse(onboardedInstitution);
                            if (Objects.nonNull(onboardedInstitution.getUserMailUuid()) && user.getWorkContact(onboardedInstitution.getUserMailUuid()) != null) {
                                response.setUserEmail(user.getWorkContact(onboardedInstitution.getUserMailUuid()).getEmail().getValue());
                            }
                            return response;
                        })
                        .toList();

        UserInfoWrapper infoWrapper = new UserInfoWrapper();
        infoWrapper.setUser(user);
        infoWrapper.setOnboardedInstitutions(onboardedInstitutionResponses);
        return infoWrapper;
    }


    @Override
    public UserDetailsWrapper getUserOnboardedProductsDetailsV2(String userId, String institutionId, String productId) {
        List<UserInstitution> usersInstitutions = userMsConnector.getUsersInstitutions(userId, institutionId, null, null, null, List.of(productId), null, null);
        UserDetailsWrapper result;
        Optional<UserInstitution> userInstitutionResponse = usersInstitutions.stream()
                .filter(userInstitution -> institutionId.equals(userInstitution.getInstitutionId()))
                .findFirst();
        List<OnboardedProductResponse> onboardedProductResponses = new ArrayList<>();
        ProductDetails productDetails = null;
        List<String> roles = new ArrayList<>();
        userInstitutionResponse.ifPresent(userInstitution -> {
            List<OnboardedProductResponse> onboardedProducts = userInstitution.getProducts().stream().filter(onboardedProductResponse -> productId.equals(onboardedProductResponse.getProductId()))
                    .filter(onboardedProductResponse -> RelationshipState.ACTIVE.name().equals(onboardedProductResponse.getStatus()))
                    .toList();
            onboardedProductResponses.addAll(onboardedProducts);
        });
        if (!onboardedProductResponses.isEmpty()) {
            onboardedProductResponses.forEach(onboardedProductResponse -> roles.add(onboardedProductResponse.getProductRole()));
            productDetails = new ProductDetails();
            productDetails.setRole(it.pagopa.selfcare.commons.base.security.PartyRole.valueOf(onboardedProductResponses.get(0).getRole()));
            productDetails.setProductId(productId);
            productDetails.setRoles(roles);
            if(onboardedProductResponses.get(0).getCreatedAt() != null) {
                productDetails.setCreatedAt(onboardedProductResponses.get(0).getCreatedAt().atZone(ZoneId.systemDefault()).toOffsetDateTime());
            }
        }
        result = UserDetailsWrapper.builder()
                .userId(userId)
                .productDetails(productDetails)
                .institutionId(institutionId)
                .build();
        return result;
    }

    @Override
    public List<OnboardedInstitutionInfo> getOnboardedInstitutionsDetails(String userId, String productId) {
        //fix temporanea per il funzionamento della getUserInfo di support
        List<UserInstitution> usersInstitutions = userMsConnector.getUsersInstitutions(userId, null, null, 350, null, Objects.isNull(productId) ? null : List.of(productId), null, null);
        List<OnboardedInstitutionInfo> onboardedInstitutionsInfo = new ArrayList<>();

        usersInstitutions.forEach(userInstitution -> {
            List<OnboardedInstitutionInfo> onboardedInstitutionResponse = msCoreConnector.getInstitutionDetails(userInstitution.getInstitutionId());

            onboardedInstitutionsInfo.addAll(onboardedInstitutionResponse.stream()
                    //Verify if userInstitution has any match with current product
                    .filter(onboardedInstitutionInfo -> userInstitution.getProducts().stream()
                            .anyMatch(onboardedProductResponse -> onboardedProductResponse.getProductId().equals(onboardedInstitutionInfo.getProductInfo().getId()))
                    )
                    .peek(onboardedInstitution -> {
                        //In case it has, Retrieve min role valid for associations with product-id
                        Optional<RelationshipState> optCurrentState = userInstitution.getProducts().stream()
                                .filter(product -> product.getProductId().equals(onboardedInstitution.getProductInfo().getId()))
                                .map(product -> RelationshipState.valueOf(product.getStatus()))
                                .min(RelationshipState::compareTo);

                        //Set role and status for min association with product
                        Optional<OnboardedProductResponse> optOnboardedProduct = optCurrentState.map(currentstate -> userInstitution.getProducts().stream()
                                        .filter(product -> product.getProductId().equals(onboardedInstitution.getProductInfo().getId()) &&
                                                product.getStatus().equals(currentstate.name())))
                                .orElse(Stream.of())
                                .findFirst();
                        optOnboardedProduct.ifPresent(item -> {
                            onboardedInstitution.getProductInfo().setRole(item.getRole());
                            onboardedInstitution.getProductInfo().setProductRole(item.getProductRole());
                            onboardedInstitution.getProductInfo().setStatus(item.getStatus());
                            onboardedInstitution.getProductInfo().setCreatedAt(Optional.ofNullable(item.getCreatedAt())
                                    .map(date -> date.atZone(java.time.ZoneId.systemDefault()).toOffsetDateTime())
                                    .orElse(null));
                        });
                        onboardedInstitution.setUserMailUuid(userInstitution.getUserMailUuid());
                    })
                    .toList());
        });

        return onboardedInstitutionsInfo;
    }

    @Override
    public List<OnboardedInstitutionResource> getOnboardedInstitutionsDetailsActive(String userId, String productId) {
        List<UserInstitution> institutionsWithProductActive = userMsConnector.getUsersInstitutions(userId, null, null, null, null, Objects.isNull(productId) ? null : List.of(productId), null, List.of(ACTIVE.name()))
                .stream()
                .filter(item -> Objects.nonNull(item.getProducts()))
                .map(userInstitution -> filterProductList(userInstitution, productId))
                .toList();

        return institutionsWithProductActive.stream()
                .map(userInstitution -> {
                    Institution institution = msCoreConnector.getInstitution(userInstitution.getInstitutionId());
                    OnboardedInstitutionResource onboardedInstitutionResource = null;
                    if(Objects.nonNull(institution) && checkInstitutionOnboardingStatus(institution, productId)) {
                        onboardedInstitutionResource = onboardingInstitutionMapper.toOnboardedInstitutionResource(institution, userInstitution, productId);
                        retrieveBilling(institution, productId, onboardedInstitutionResource);
                    }
                    return onboardedInstitutionResource;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private boolean checkInstitutionOnboardingStatus(Institution institution, String productId) {
        return institution.getOnboarding().stream()
                .anyMatch(onboarding -> productId.equalsIgnoreCase(onboarding.getProductId())
                        && RelationshipState.ACTIVE.equals(onboarding.getStatus()));
    }

    private void retrieveBilling(Institution institution, String productId, OnboardedInstitutionResource onboardedInstitutionResource){
        List<Billing> billingList = new ArrayList<>();

        institution.getOnboarding().forEach(onboarding -> {
            if (productId.equalsIgnoreCase(onboarding.getProductId())) {
                if (Objects.nonNull(onboarding.getBilling())) {
                    billingList.add(onboarding.getBilling());
                } else if (Objects.nonNull(institution.getBilling())) {
                    billingList.add(institution.getBilling());
                }
            }
        });

        Billing billing = billingList.stream().findFirst().orElse(null);

        if (Objects.nonNull(billing)) {
            onboardedInstitutionResource.setTaxCodeInvoicing(billing.getTaxCodeInvoicing());
            onboardedInstitutionResource.setRecipientCode(billing.getRecipientCode());
        }
    }

    private UserInstitution filterProductList(UserInstitution userInstitution, String productId) {
        if (StringUtils.hasText(productId)) {
            userInstitution.setProducts(userInstitution.getProducts().stream()
                    .filter(product -> product.getProductId().equals(productId) && RelationshipState.ACTIVE.name().equals(product.getStatus()))
                    .toList());
        }

        return userInstitution;
    }

    @Override
    public List<UserInstitution> getUsersInstitutions(String userId, String institutionId, Integer page, Integer size, List<String> productRoles, List<String> products, List<PartyRole> roles, List<String> states){
        return userMsConnector.getUsersInstitutions(userId,institutionId, page, size, productRoles, products, roles, states);
    }
}
