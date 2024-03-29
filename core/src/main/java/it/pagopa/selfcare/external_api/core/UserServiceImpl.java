package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.external_api.api.MsCoreConnector;
import it.pagopa.selfcare.external_api.api.UserRegistryConnector;
import it.pagopa.selfcare.external_api.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionResponse;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingInfoResponse;
import it.pagopa.selfcare.external_api.model.onboarding.ProductInfo;
import it.pagopa.selfcare.external_api.model.user.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static it.pagopa.selfcare.external_api.model.user.User.Fields.*;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private static final EnumSet<User.Fields> USER_FIELD_LIST = EnumSet.of(name, familyName, workContacts);
    private static final List<RelationshipState> DEFAULT_USER_STATUSES =  new ArrayList<>(Arrays.asList(RelationshipState.values()));
    private final UserRegistryConnector userRegistryConnector;
    private final MsCoreConnector msCoreConnector;

    @Autowired
    public UserServiceImpl(UserRegistryConnector userRegistryConnector,
                           MsCoreConnector msCoreConnector) {
        this.userRegistryConnector = userRegistryConnector;
        this.msCoreConnector = msCoreConnector;
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

}
