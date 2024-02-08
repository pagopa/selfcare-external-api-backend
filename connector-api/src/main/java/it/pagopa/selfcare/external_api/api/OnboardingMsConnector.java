package it.pagopa.selfcare.external_api.api;

import it.pagopa.selfcare.external_api.model.onboarding.OnboardingData;
import it.pagopa.selfcare.external_api.model.token.Token;

import java.util.List;

public interface OnboardingMsConnector {

    List<Token> getToken(String onboardingId);
    void onboarding(OnboardingData onboardingData);
    List<Token> getOnboardings(String productId, int page, int size);

}
