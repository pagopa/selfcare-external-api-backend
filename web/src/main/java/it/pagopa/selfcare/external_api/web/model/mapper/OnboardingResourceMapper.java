package it.pagopa.selfcare.external_api.web.model.mapper;

import it.pagopa.selfcare.external_api.model.onboarding.OnboardingData;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingUsersRequest;
import it.pagopa.selfcare.external_api.model.onboarding.PdaOnboardingData;
import it.pagopa.selfcare.external_api.web.model.onboarding.OnboardingInstitutionUsersRequest;
import it.pagopa.selfcare.external_api.web.model.onboarding.OnboardingProductDto;
import it.pagopa.selfcare.external_api.web.model.onboarding.PdaOnboardingDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OnboardingResourceMapper {

    @Mapping(source = "billingData", target = "billing")
    @Mapping(source = "institutionLocationData", target = "location")
    @Mapping(source = "billingData.businessName", target = "institutionUpdate.description")
    @Mapping(source = "billingData.registeredOffice", target = "institutionUpdate.address")
    @Mapping(source = "pspData", target = "institutionUpdate.paymentServiceProvider")
    @Mapping(source = "pspData.dpoData", target = "institutionUpdate.dataProtectionOfficer")
    @Mapping(source = "geographicTaxonomies", target = "institutionUpdate.geographicTaxonomies")
    @Mapping(source = "companyInformations.rea", target = "institutionUpdate.rea")
    @Mapping(source = "companyInformations.shareCapital", target = "institutionUpdate.shareCapital")
    @Mapping(source = "companyInformations.businessRegisterPlace", target = "institutionUpdate.businessRegisterPlace")
    @Mapping(source = "assistanceContacts.supportEmail", target = "institutionUpdate.supportEmail")
    @Mapping(source = "assistanceContacts.supportPhone", target = "institutionUpdate.supportPhone")
    OnboardingData toEntity(OnboardingProductDto dto);

    @Mapping(source = "taxCode", target = "institutionExternalId")
    @Mapping(source = "taxCode", target = "taxCode")
    @Mapping(source = "businessName", target = "description")
    @Mapping(source = "vatNumber", target = "billing.vatNumber")
    @Mapping(source = "recipientCode", target = "billing.recipientCode")
    PdaOnboardingData toEntity(PdaOnboardingDto dto);


    OnboardingUsersRequest toOnboardingUsersRequest(OnboardingInstitutionUsersRequest request);
}