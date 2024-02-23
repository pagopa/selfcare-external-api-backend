package it.pagopa.selfcare.external_api.api;

import it.pagopa.selfcare.external_api.model.onboarding.OnboardingData;
import it.pagopa.selfcare.external_api.model.token.Token;
import it.pagopa.selfcare.external_api.model.token.TokenOnboardedUsers;

import java.util.List;

public interface OnboardingMsConnector {

    List<Token> getToken(String onboardingId);
    void onboarding(OnboardingData onboardingData);
    void onboardingImportPA(OnboardingData onboardingData);
    List<TokenOnboardedUsers> getOnboardings(String productId, int page, int size);

}
