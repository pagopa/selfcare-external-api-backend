package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.external_api.api.OnboardingMsConnector;
import it.pagopa.selfcare.external_api.model.token.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class TokenServiceImpl implements TokenService {

    private final OnboardingMsConnector onboardingMsConnector;
    private final Ms

    @Autowired
    TokenServiceImpl(OnboardingMsConnector onboardingMsConnector) {
        this.onboardingMsConnector = onboardingMsConnector;
    }

    @Override
    public List findByProductId(String productId, int page, int size) {
        log.trace("findByProductId start");
        log.debug("findByProductId parameter: {}", productId);
        List<Token> tokens = onboardingMsConnector.getOnboardings(productId, page, size);

        log.trace("findByProductId end");
        return tokens;
    }
}
