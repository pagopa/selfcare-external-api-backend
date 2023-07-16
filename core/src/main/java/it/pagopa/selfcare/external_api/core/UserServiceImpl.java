package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.external_api.api.MsCoreConnector;
import it.pagopa.selfcare.external_api.api.UserRegistryConnector;
import it.pagopa.selfcare.external_api.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingInfoResponse;
import it.pagopa.selfcare.external_api.model.user.User;
import it.pagopa.selfcare.external_api.model.user.UserInfoWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Optional;

import static it.pagopa.selfcare.external_api.model.user.User.Fields.*;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private static final EnumSet<User.Fields> USER_FIELD_LIST = EnumSet.of(name, familyName, workContacts);
    private final UserRegistryConnector userRegistryConnector;
    private final MsCoreConnector msCoreConnector;
    private final JwtService jwtExternalService;

    @Autowired
    public UserServiceImpl(UserRegistryConnector userRegistryConnector,
                           MsCoreConnector msCoreConnector,
                           JwtService jwtExternalService) {
        this.userRegistryConnector = userRegistryConnector;
        this.msCoreConnector = msCoreConnector;
        this.jwtExternalService = jwtExternalService;
    }

    @Override
    public UserInfoWrapper getUserInfo(String fiscalCode) {

        log.trace("geUserInfo start");
        log.debug("geUserInfo fiscalCode = {}", fiscalCode);
        final Optional<User> searchResult = userRegistryConnector.search(fiscalCode, USER_FIELD_LIST);
        if(searchResult.isEmpty()) {
            throw new ResourceNotFoundException("User with fiscal code " + fiscalCode + " not found");
        }

        //Add user uuid into claims
        User user = searchResult.get();
        jwtExternalService.putUserIntoSecurityContext(user);

        OnboardingInfoResponse onboardingInfoResponse = msCoreConnector.getOnboardingInfo();
        UserInfoWrapper result = UserInfoWrapper.builder()
                .userInfo(user)
                .onboardedInstitutions(onboardingInfoResponse.getInstitutions())
                .build();

        log.debug("geUserInfo result = {}", result);
        log.trace("geUserInfo end");
        return result;
    }

}
