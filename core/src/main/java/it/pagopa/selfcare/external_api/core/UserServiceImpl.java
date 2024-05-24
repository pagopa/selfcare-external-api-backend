package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.external_api.api.MsCoreConnector;
import it.pagopa.selfcare.external_api.api.UserMsConnector;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionInfo;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionResponse;
import it.pagopa.selfcare.external_api.model.onboarding.mapper.OnboardingInstitutionMapper;
import it.pagopa.selfcare.external_api.model.user.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Stream;

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
            productDetails.setRole(PartyRole.valueOf(onboardedProductResponses.get(0).getRole()));
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
        List<UserInstitution> usersInstitutions = userMsConnector.getUsersInstitutions(userId, null, null, null, null, Objects.isNull(productId) ? null : List.of(productId), null, null);
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
    public List<OnboardedInstitutionInfo> getOnboardedInstitutionsDetailsActive(String userId, String productId) {
        List<UserInstitution> institutionsWithProductActive = userMsConnector.getUsersInstitutions(userId, null, null, null, null, Objects.isNull(productId) ? null : List.of(productId), null, null)
                .stream()
                .filter(item -> Objects.nonNull(item.getProducts()))
                .filter(item -> item.getProducts().stream()
                        .filter(product -> Objects.nonNull(product.getProductId()))
                        .anyMatch(product -> product.getProductId().equals(productId) && RelationshipState.ACTIVE.name().equals(product.getStatus())))
                .peek(item -> item.setProducts(item.getProducts().stream()
                        .filter(product -> product.getProductId().equals(productId) && RelationshipState.ACTIVE.name().equals(product.getStatus()))
                        .toList()))
                .toList();

        List<OnboardedInstitutionInfo> onboardedInstitutionsInfo = new ArrayList<>();

        institutionsWithProductActive
                .forEach(institution -> {
                    List<OnboardedInstitutionInfo> institutionOnboardings = msCoreConnector.getInstitutionDetails(institution.getInstitutionId());
                    onboardedInstitutionsInfo.addAll(institutionOnboardings.stream()
                            .filter(onboardedInstitution -> RelationshipState.ACTIVE.name().equals(onboardedInstitution.getState()))
                            .filter(onboardedInstitutionInfo -> institution.getProducts().stream()
                                    .anyMatch(product -> product.getProductId().equals(onboardedInstitutionInfo.getProductInfo().getId()))
                            )
                            .peek(onboardedInstitution -> {
                                onboardedInstitution.getProductInfo().setRole(institution.getProducts().stream()
                                        .filter(product -> product.getProductId().equals(onboardedInstitution.getProductInfo().getId()) &&
                                                product.getStatus().equals(onboardedInstitution.getProductInfo().getStatus()))
                                        .map(OnboardedProductResponse::getProductRole).findFirst().orElse(null));
                                onboardedInstitution.setUserMailUuid(institution.getUserMailUuid());
                            })
                            .toList());
                });

        return onboardedInstitutionsInfo;
    }
}
