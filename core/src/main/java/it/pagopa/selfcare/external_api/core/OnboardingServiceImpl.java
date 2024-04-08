package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.external_api.api.*;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
class OnboardingServiceImpl implements OnboardingService {

    private final OnboardingMsConnector onboardingMsConnector;

    @Override
    public void oldContractOnboardingV2(OnboardingData onboardingImportData) {
        log.trace("oldContractOnboarding start");
        log.debug("oldContractOnboarding = {}", onboardingImportData);
        onboardingMsConnector.onboardingImportPA(onboardingImportData);
        log.trace("oldContractOnboarding end");
    }

    @Override
    public void autoApprovalOnboardingProductV2(OnboardingData onboardingData) {
        log.trace("autoApprovalOnboarding start");
        log.debug("autoApprovalOnboarding = {}", onboardingData);
        onboardingMsConnector.onboarding(onboardingData);
        log.trace("autoApprovalOnboarding end");
    }
}