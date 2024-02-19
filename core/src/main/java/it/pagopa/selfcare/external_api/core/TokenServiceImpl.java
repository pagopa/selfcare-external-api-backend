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

    @Autowired
    TokenServiceImpl(OnboardingMsConnector onboardingMsConnector) {
        this.onboardingMsConnector = onboardingMsConnector;
    }

    @Override
    public List<TokenOnboardedUsers> findByProductId(String productId, int page, int size) {
        log.trace("findByProductId start");
        log.debug("findByProductId parameter: {}", productId);
        final List<TokenOnboardedUsers> tokens = onboardingMsConnector.getOnboardings(productId, page, size);
        log.trace("findByProductId end");
        return tokens;
    }
}
