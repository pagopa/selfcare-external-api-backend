package it.pagopa.selfcare.external_api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingData;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingUsersRequest;
import it.pagopa.selfcare.external_api.model.user.RelationshipInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface OnboardingService {

    void oldContractOnboardingV2(OnboardingData onboardingData);

    void autoApprovalOnboardingProductV2(OnboardingData onboardingData, MultipartFile contract) throws JsonProcessingException;

    void autoApprovalOnboardingImportProductV2(OnboardingData onboardingData);

    List<RelationshipInfo> onboardingUsers(
            OnboardingUsersRequest onboardingUsersRequest, String userName, String surname);
}
