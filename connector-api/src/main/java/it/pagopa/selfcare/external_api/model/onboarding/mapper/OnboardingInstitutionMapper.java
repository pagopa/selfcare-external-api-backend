package it.pagopa.selfcare.external_api.model.onboarding.mapper;

import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionInfo;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")

public interface OnboardingInstitutionMapper {
    OnboardedInstitutionResponse toOnboardedInstitutionResponse(OnboardedInstitutionInfo onboardedInstitutionInfo);
}
