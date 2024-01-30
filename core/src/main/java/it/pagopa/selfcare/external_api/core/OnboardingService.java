package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.external_api.model.onboarding.OnboardingData;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingImportData;
import it.pagopa.selfcare.external_api.model.onboarding.PdaOnboardingData;

public interface OnboardingService {

    void oldContractOnboarding(OnboardingImportData onboardingData);

    void autoApprovalOnboarding(OnboardingData onboardingData);

    void autoApprovalOnboardingFromPda(PdaOnboardingData onboardingData, String injestionInstitutionType);

    void autoApprovalOnboardingProduct(OnboardingData onboardingData);
}
