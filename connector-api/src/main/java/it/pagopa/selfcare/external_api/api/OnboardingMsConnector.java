package it.pagopa.selfcare.external_api.api;

import it.pagopa.selfcare.external_api.model.token.Token;

import java.util.List;

public interface OnboardingMsConnector {

    List<Token> getToken(String onboardingId);
}
