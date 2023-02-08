package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.external_api.model.onboarding.OnboardingImportData;

public interface OnboardingService {

    void oldContractOnboarding(OnboardingImportData onboardingData);

}
