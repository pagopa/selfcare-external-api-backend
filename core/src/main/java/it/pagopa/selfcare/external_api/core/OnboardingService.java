package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.external_api.model.onboarding.OnboardingData;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingImportData;
import it.pagopa.selfcare.external_api.model.onboarding.PdaOnboardingData;

public interface OnboardingService {

    void oldContractOnboardingV2(OnboardingData onboardingData);

    void autoApprovalOnboardingProductV2(OnboardingData onboardingData);
}
