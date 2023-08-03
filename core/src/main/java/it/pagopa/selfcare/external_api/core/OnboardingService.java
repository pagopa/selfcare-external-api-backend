package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.external_api.model.onboarding.OnboardingData;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingImportData;
import org.springframework.http.ResponseEntity;

public interface OnboardingService {

    void oldContractOnboarding(OnboardingImportData onboardingData);

    void autoApprovalOnboarding(OnboardingData onboardingData);

    void autoApprovalOnboardingProduct(OnboardingData onboardingData);

    ResponseEntity<Void> verifyOnboarding(String externalInstitutionId, String productId);
}
