package it.pagopa.selfcare.external_api.mapper;

import it.pagopa.selfcare.external_api.model.onboarding.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Mapper(componentModel = "spring")
public interface OnboardingResourceMapper {

  @Mapping(source = "billingData", target = "billing")
  @Mapping(source = "institutionLocationData", target = "location")
  @Mapping(source = "billingData.businessName", target = "institutionUpdate.description")
  @Mapping(source = "billingData.registeredOffice", target = "institutionUpdate.address")
  @Mapping(source = "pspData", target = "institutionUpdate.paymentServiceProvider")
  @Mapping(source = "pspData.dpoData", target = "institutionUpdate.dataProtectionOfficer")
  @Mapping(source = "gpuData", target = "institutionUpdate.gpuData")
  @Mapping(source = "geographicTaxonomies", target = "institutionUpdate.geographicTaxonomies")
  @Mapping(source = "companyInformations.rea", target = "institutionUpdate.rea")
  @Mapping(source = "companyInformations.shareCapital", target = "institutionUpdate.shareCapital")
  @Mapping(
      source = "companyInformations.businessRegisterPlace",
      target = "institutionUpdate.businessRegisterPlace")
  @Mapping(source = "assistanceContacts.supportEmail", target = "institutionUpdate.supportEmail")
  @Mapping(source = "assistanceContacts.supportPhone", target = "institutionUpdate.supportPhone")
  OnboardingData toEntity(OnboardingProductDto dto);

  @Mapping(source = "billingData", target = "billing")
  @Mapping(source = "institutionLocationData", target = "location")
  @Mapping(source = "billingData.businessName", target = "institutionUpdate.description")
  @Mapping(source = "billingData.registeredOffice", target = "institutionUpdate.address")
  @Mapping(source = "pspData", target = "institutionUpdate.paymentServiceProvider")
  @Mapping(source = "pspData.dpoData", target = "institutionUpdate.dataProtectionOfficer")
  @Mapping(source = "geographicTaxonomies", target = "institutionUpdate.geographicTaxonomies")
  @Mapping(source = "companyInformations.rea", target = "institutionUpdate.rea")
  @Mapping(source = "companyInformations.shareCapital", target = "institutionUpdate.shareCapital")
  @Mapping(
      source = "companyInformations.businessRegisterPlace",
      target = "institutionUpdate.businessRegisterPlace")
  @Mapping(source = "assistanceContacts.supportEmail", target = "institutionUpdate.supportEmail")
  @Mapping(source = "assistanceContacts.supportPhone", target = "institutionUpdate.supportPhone")
  @Mapping(source = "activatedAt", target = "contractImported.activatedAt")
  @Mapping(source = "contractSigned", target = "contractImported.filePath")
  OnboardingData toEntity(OnboardingImportProductDto dto);

  OnboardingUsersRequest toOnboardingUsersRequest(OnboardingInstitutionUsersRequest request);
}
