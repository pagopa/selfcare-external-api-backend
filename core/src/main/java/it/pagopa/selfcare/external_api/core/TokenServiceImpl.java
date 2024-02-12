package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.external_api.api.MsCoreConnector;
import it.pagopa.selfcare.external_api.api.OnboardingMsConnector;
import it.pagopa.selfcare.external_api.model.token.Token;
import it.pagopa.selfcare.external_api.model.token.TokenOnboardedUsers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TokenServiceImpl implements TokenService {

    private final OnboardingMsConnector onboardingMsConnector;
    private final MsCoreConnector msCoreConnector;

    @Autowired
    TokenServiceImpl(OnboardingMsConnector onboardingMsConnector,
                     MsCoreConnector msCoreConnector) {
        this.onboardingMsConnector = onboardingMsConnector;
        this.msCoreConnector = msCoreConnector;
    }

    @Override
    public List<TokenOnboardedUsers> findByProductId(String productId, int page, int size) {
        log.trace("findByProductId start");
        log.debug("findByProductId parameter: {}", productId);
        final List<TokenOnboardedUsers> tokens = onboardingMsConnector.getOnboardings(productId, page, size);
        tokens.forEach(token -> {
            try {
                token.setOnboardedUsers(msCoreConnector.getOnboarderUsers(token.getUsers()));
            } catch (Exception e) {
                log.debug("Impossible to retrieve users for token with ID: {}", token.getId());
                token.setOnboardedUsers(Collections.emptyList());
            }
        });
        log.trace("findByProductId end");
        return tokens;
    }
}
