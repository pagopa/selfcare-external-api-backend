package it.pagopa.selfcare.external_api.api;

import it.pagopa.selfcare.external_api.model.onboarding.OnboardingInfoResponse;
import it.pagopa.selfcare.external_api.model.pnpg.CreatePnPgInstitution;
import it.pagopa.selfcare.external_api.model.token.Token;
import it.pagopa.selfcare.external_api.model.user.RelationshipState;

import java.util.List;

public interface MsCoreConnector {

    String createPnPgInstitution(CreatePnPgInstitution request);
    Token getToken(String institutionId, String productId);
    OnboardingInfoResponse getInstitutionProductsInfo(String userId);
    OnboardingInfoResponse getInstitutionProductsInfo(String userId, List<RelationshipState> userStatuses);

}
