package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.external_api.model.onboarding.OnboardingData;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingUsersRequest;
import it.pagopa.selfcare.external_api.model.user.RelationshipInfo;

import java.util.List;

public interface OnboardingService {

    void oldContractOnboardingV2(OnboardingData onboardingData);

    void autoApprovalOnboardingProductV2(OnboardingData onboardingData);

    List<RelationshipInfo> onboardingUsers(OnboardingUsersRequest onboardingUsersRequest, String userName, String surname);
}
