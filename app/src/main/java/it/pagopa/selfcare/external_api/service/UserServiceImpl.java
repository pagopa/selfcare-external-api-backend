package it.pagopa.selfcare.external_api.service;

import it.pagopa.selfcare.core.generated.openapi.v1.dto.InstitutionResponse;
import it.pagopa.selfcare.external_api.client.MsCoreInstitutionApiClient;
import it.pagopa.selfcare.external_api.client.MsUserApiRestClient;
import it.pagopa.selfcare.external_api.mapper.InstitutionMapper;
import it.pagopa.selfcare.external_api.mapper.UserMapper;
import it.pagopa.selfcare.external_api.model.institution.Institution;
import it.pagopa.selfcare.external_api.model.onboarding.Billing;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionInfo;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionResource;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionResponse;
import it.pagopa.selfcare.external_api.model.onboarding.mapper.OnboardingInstitutionMapper;
import it.pagopa.selfcare.external_api.model.user.*;
import it.pagopa.selfcare.onboarding.common.PartyRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Stream;

import static it.pagopa.selfcare.external_api.model.product.ProductOnboardingStatus.ACTIVE;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final OnboardingInstitutionMapper onboardingInstitutionMapper;
    private final InstitutionMapper institutionMapper;
    private final MsCoreInstitutionApiClient institutionApiClient;
    private final UserMapper userMapper;
    private final MsUserApiRestClient msUserApiRestClient;


    @Override
    public UserInfoWrapper getUserInfoV2(String fiscalCode, List<RelationshipState> userStatuses) {
        log.trace("geUserInfo start");
        it.pagopa.selfcare.user.generated.openapi.v1.dto.SearchUserDto searchUserDto = new it.pagopa.selfcare.user.generated.openapi.v1.dto.SearchUserDto(fiscalCode);
        final User user = userMapper.toUserFromUserDetailResponse(msUserApiRestClient._usersSearchPost(null, searchUserDto).getBody());
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
        List<UserInstitution> usersInstitutions =  Objects.requireNonNull(msUserApiRestClient._usersGet(
                        institutionId, null, null, List.of(productId), null
                        , null, null, userId).getBody())
                .stream().map(userMapper::toUserInstitutionsFromUserInstitutionResponse).toList();
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

        List<UserInstitution> usersInstitutions = Objects.requireNonNull(msUserApiRestClient._usersGet(
                        null, null, null,  Objects.isNull(productId) ? null : List.of(productId), null
                        , 350, null, userId).getBody())
                .stream().map(userMapper::toUserInstitutionsFromUserInstitutionResponse).toList();

        List<OnboardedInstitutionInfo> onboardedInstitutionsInfo = new ArrayList<>();

        usersInstitutions.forEach(userInstitution -> {
            List<OnboardedInstitutionInfo> onboardedInstitutionResponse = getInstitutionDetails(userInstitution.getInstitutionId());

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
                                    .map(date -> date.atZone(ZoneId.systemDefault()).toOffsetDateTime())
                                    .orElse(null));
                        });
                        onboardedInstitution.setUserMailUuid(userInstitution.getUserMailUuid());
                    })
                    .toList());
        });

        return onboardedInstitutionsInfo;
    }

    private List<OnboardedInstitutionInfo> getInstitutionDetails(String institutionId) {
            ResponseEntity<InstitutionResponse> responseEntity = institutionApiClient._retrieveInstitutionByIdUsingGET(institutionId);
            if (Objects.nonNull(responseEntity) && Objects.nonNull(responseEntity.getBody()) && Objects.nonNull(responseEntity.getBody().getOnboarding())) {
                return responseEntity.getBody().getOnboarding().stream().map(onboardedProductResponse -> {
                    OnboardedInstitutionInfo onboardedInstitutionInfo = institutionMapper.toOnboardedInstitution(responseEntity.getBody());
                    it.pagopa.selfcare.external_api.model.onboarding.ProductInfo productInfo = institutionMapper.toProductInfo(onboardedProductResponse);
                    onboardedInstitutionInfo.setProductInfo(productInfo);
                    onboardedInstitutionInfo.setState(productInfo.getStatus());
                    return onboardedInstitutionInfo;
                }).toList();
            }
            return Collections.emptyList();
    }

    @Override
    public List<OnboardedInstitutionResource> getOnboardedInstitutionsDetailsActive(String userId, String productId) {

        List<UserInstitution> institutionsWithProductActive = Objects.requireNonNull(msUserApiRestClient._usersGet(
                        null, null, null, Objects.isNull(productId) ? null : List.of(productId), null
                        , null, List.of(ACTIVE.name()), userId).getBody())
                .stream().map(userMapper::toUserInstitutionsFromUserInstitutionResponse)
                .filter(item -> Objects.nonNull(item.getProducts()))
                .toList();

        return institutionsWithProductActive.stream()
                .map(userInstitution -> {
                    Institution institution =  institutionMapper.toInstitution(Objects.requireNonNull(institutionApiClient._retrieveInstitutionByIdUsingGET(userInstitution.getInstitutionId()))
                            .getBody());
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

    @Override
    public List<UserInstitution> getUsersInstitutions(String userId, String institutionId, Integer page, Integer size, List<String> productRoles, List<String> products, List<PartyRole> roles, List<String> states){
        return Objects.requireNonNull(msUserApiRestClient._usersGet(
                        institutionId, page, productRoles, products, toDtoPartyRole(roles)
                        , size, states, userId).getBody())
                .stream().map(userMapper::toUserInstitutionsFromUserInstitutionResponse).toList();
    }

    private List<it.pagopa.selfcare.user.generated.openapi.v1.dto.PartyRole> toDtoPartyRole(List<PartyRole> roles) {
        List<it.pagopa.selfcare.user.generated.openapi.v1.dto.PartyRole> partyRoles = new ArrayList<>();
        if (roles != null) {
            roles.forEach(partyRole -> {
                it.pagopa.selfcare.user.generated.openapi.v1.dto.PartyRole role = it.pagopa.selfcare.user.generated.openapi.v1.dto.PartyRole.valueOf(partyRole.name());
                partyRoles.add(role);
            });
        } else {
            return Collections.emptyList();
        }
        return partyRoles;
    }
}