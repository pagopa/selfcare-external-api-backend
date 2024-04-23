package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.external_api.api.MsCoreConnector;
import it.pagopa.selfcare.external_api.api.UserMsConnector;
import it.pagopa.selfcare.external_api.api.UserRegistryConnector;
import it.pagopa.selfcare.external_api.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionInfo;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionResponse;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingInfoResponse;
import it.pagopa.selfcare.external_api.model.onboarding.ProductInfo;
import it.pagopa.selfcare.external_api.model.onboarding.mapper.OnboardingInstitutionMapper;
import it.pagopa.selfcare.external_api.model.user.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.external_api.model.user.User.Fields.*;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private static final EnumSet<User.Fields> USER_FIELD_LIST = EnumSet.of(name, familyName, workContacts);
    private static final List<RelationshipState> DEFAULT_USER_STATUSES =  new ArrayList<>(Arrays.asList(RelationshipState.values()));
    private final UserRegistryConnector userRegistryConnector;
    private final MsCoreConnector msCoreConnector;
    private final UserMsConnector userMsConnector;
    private final OnboardingInstitutionMapper onboardingInstitutionMapper;

    @Autowired
    public UserServiceImpl(UserRegistryConnector userRegistryConnector,
                           MsCoreConnector msCoreConnector, UserMsConnector userMsConnector, OnboardingInstitutionMapper onboardingInstitutionMapper) {
        this.userRegistryConnector = userRegistryConnector;
        this.msCoreConnector = msCoreConnector;
        this.userMsConnector = userMsConnector;
        this.onboardingInstitutionMapper = onboardingInstitutionMapper;
    }

    @Override
    public UserInfoWrapper getUserInfo(String fiscalCode, List<RelationshipState> userStatuses) {

        log.trace("geUserInfo start");
        log.debug("geUserInfo fiscalCode = {}", fiscalCode);
        final Optional<User> searchResult = userRegistryConnector.search(fiscalCode, USER_FIELD_LIST);
        if (searchResult.isEmpty()) {
            throw new ResourceNotFoundException("User with fiscal code " + fiscalCode + " not found");
        }

        User user = searchResult.get();

        OnboardingInfoResponse onboardingInfoResponse = msCoreConnector.getInstitutionProductsInfo(user.getId(), Objects.nonNull(userStatuses) ? userStatuses : DEFAULT_USER_STATUSES);
        onboardingInfoResponse.getInstitutions().forEach(obj -> {
            if(Objects.nonNull(user.getWorkContacts()) && user.getWorkContacts().containsKey(obj.getId()))
                obj.setUserEmail(user.getWorkContact(obj.getId()).getEmail().getValue());
        });

        UserInfoWrapper result = UserInfoWrapper.builder()
                .user(user)
                .onboardedInstitutions(onboardingInfoResponse.getInstitutions())
                .build();

        log.debug("geUserInfo result = {}", result);
        log.trace("geUserInfo end");
        return result;
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
                        .filter(institution -> userStatusesString.isEmpty() || userStatusesString.contains(institution.getState()))
                        .map(onboardedInstitution -> {
                            OnboardedInstitutionResponse response = onboardingInstitutionMapper.toOnboardedInstitutionResponse(onboardedInstitution);
                            if(user.getWorkContact(onboardedInstitution.getUserMailUuid()) != null){
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
    public UserDetailsWrapper getUserOnboardedProductDetails(String userId, String institutionId, String productId) {
        log.trace("getUserOnboardedProductDetails start");
        log.debug("getUserOnboardedProductDetails userId = {}, institutionId = {}, productId = {}", userId, institutionId, productId);
        UserDetailsWrapper result;
        OnboardingInfoResponse onboardingInfoResponse = msCoreConnector.getInstitutionProductsInfo(userId);
        Optional<OnboardedInstitutionResponse> institutionResponse = onboardingInfoResponse.getInstitutions().stream()
                .filter(onboardedInstitutionResponse -> institutionId.equals(onboardedInstitutionResponse.getId())
                        && productId.equals(onboardedInstitutionResponse.getProductInfo().getId()))
                .findFirst();
        ProductDetails productDetails = null;
        if (institutionResponse.isPresent()) {
            OnboardedInstitutionResponse institution = institutionResponse.get();
            ProductInfo productInfo = institution.getProductInfo();
            productDetails = ProductDetails.builder()
                    .roles(List.of(productInfo.getRole()))
                    .createdAt(productInfo.getCreatedAt())
                    .role(institution.getRole())
                    .productId(productId)
                    .build();

        }
        result = UserDetailsWrapper.builder()
                .userId(userId)
                .institutionId(institutionId)
                .productDetails(productDetails)
                .build();
        log.debug("getUserOnboardedProductDetails result = {}", result);
        log.trace("getUserOnboardedProductDetails end");
        return result;
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
            onboardedProductResponses.forEach(onboardedProductResponse -> {
                roles.add(onboardedProductResponse.getProductRole());
            });
            productDetails = new ProductDetails();
            productDetails.setRole(PartyRole.valueOf(onboardedProductResponses.get(0).getRole()));
            productDetails.setProductId(productId);
            productDetails.setRoles(roles);
            productDetails.setCreatedAt(onboardedProductResponses.get(0).getCreatedAt().atZone(ZoneId.systemDefault()).toOffsetDateTime());
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
        List<UserInstitution> institutions = userMsConnector.getUsersInstitutions(userId, null, null, null, null, Objects.isNull(productId) ? null : List.of(productId), null, null);
        List<OnboardedInstitutionInfo> onboardedInstitutionsInfo = new ArrayList<>();

        institutions.forEach(institution -> {
            List<OnboardedInstitutionInfo> onboardedInstitutionResponse = msCoreConnector.getInstitutionDetails(institution.getInstitutionId());
            onboardedInstitutionsInfo.addAll(onboardedInstitutionResponse.stream()
                    .filter(onboardedInstitution -> onboardedInstitution.getId().equals(institution.getInstitutionId()))
                    .filter(onboardedInstitutionInfo -> institution.getProducts().stream()
                            .anyMatch(onboardedProductResponse -> onboardedProductResponse.getProductId().equals(onboardedInstitutionInfo.getProductInfo().getId()))
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
