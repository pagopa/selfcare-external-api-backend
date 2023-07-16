package it.pagopa.selfcare.external_api.api;

import it.pagopa.selfcare.external_api.model.onboarding.OnboardingInfoResponse;
import it.pagopa.selfcare.external_api.model.pnpg.CreatePnPgInstitution;
import it.pagopa.selfcare.external_api.model.token.Token;

public interface MsCoreConnector {

    String createPnPgInstitution(CreatePnPgInstitution request);
    Token getToken(String institutionId, String productId);
    OnboardingInfoResponse getOnboardingInfo();

}
