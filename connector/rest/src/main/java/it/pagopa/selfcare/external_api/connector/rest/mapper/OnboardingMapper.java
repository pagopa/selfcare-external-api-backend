package it.pagopa.selfcare.external_api.connector.rest.mapper;


import it.pagopa.selfcare.core.generated.openapi.v1.dto.OnboardedProductResponse;
import it.pagopa.selfcare.external_api.model.institutions.GeographicTaxonomy;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.institutions.Onboarding;
import it.pagopa.selfcare.external_api.model.onboarding.Billing;
import it.pagopa.selfcare.external_api.model.onboarding.DataProtectionOfficer;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingData;
import it.pagopa.selfcare.external_api.model.onboarding.PaymentServiceProvider;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface OnboardingMapper {

    @Mapping(target = "institution", source = ".", qualifiedByName = "toInstitutionBase")
    OnboardingPaRequest toOnboardingPaRequest(OnboardingData onboardingData);
    @Mapping(target = "institution", source = ".", qualifiedByName = "toInstitutionPsp")
    OnboardingPspRequest toOnboardingPspRequest(OnboardingData onboardingData);
    @Mapping(target = "institution", source = ".", qualifiedByName = "toInstitutionBase")
    OnboardingDefaultRequest toOnboardingDefaultRequest(OnboardingData onboardingData);
    @Mapping(target = "institution", source = "institutionUpdate")
    @Mapping(target = "institution.institutionType", ignore = true)
    @Mapping(target = "contractImported.createdAt", source = "contractImported.createdAt", qualifiedByName = "convertDate")
    OnboardingImportRequest toOnboardingImportRequest(OnboardingData onboardingData);
    GeographicTaxonomyDto toGeographicTaxonomyDto(GeographicTaxonomy geographicTaxonomy);

    @Named("convertDate")
    default LocalDateTime convertDate(OffsetDateTime date) {
        return date.toLocalDateTime();
    }
    @Named("toInstitutionBase")
    default it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.InstitutionBaseRequest toInstitutionBase(OnboardingData onboardingData) {
        InstitutionBaseRequest institution = new InstitutionBaseRequest();
        institution.institutionType(InstitutionType.valueOf(onboardingData.getInstitutionType().name()));
        institution.taxCode(onboardingData.getTaxCode());
        institution.subunitCode(onboardingData.getSubunitCode());
        institution.subunitType(Optional.ofNullable(onboardingData.getSubunitType())
                .map(it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.InstitutionPaSubunitType::valueOf)
                .orElse(null));
        institution.setOrigin(Optional.ofNullable(onboardingData.getOrigin()).map(it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.Origin::fromValue).orElse(null));
        institution.setOriginId(onboardingData.getOriginId());
        if(Objects.nonNull(onboardingData.getLocation())) {
            institution.setCity(onboardingData.getLocation().getCity());
            institution.setCountry(onboardingData.getLocation().getCountry());
            institution.setCounty(onboardingData.getLocation().getCounty());
        }
        institution.setDescription(onboardingData.getInstitutionUpdate().getDescription());
        institution.digitalAddress(onboardingData.getInstitutionUpdate().getDigitalAddress());
        institution.address(onboardingData.getInstitutionUpdate().getAddress());
        institution.zipCode(onboardingData.getInstitutionUpdate().getZipCode());
        institution.geographicTaxonomies(Optional.ofNullable(onboardingData.getInstitutionUpdate().getGeographicTaxonomies())
                .map(geotaxes -> geotaxes.stream()
                        .map(this::toGeographicTaxonomyDto)
                        .collect(Collectors.toList()))
                .orElse(null));
        institution.rea(onboardingData.getInstitutionUpdate().getRea());
        institution.shareCapital(onboardingData.getInstitutionUpdate().getShareCapital());
        institution.businessRegisterPlace(onboardingData.getInstitutionUpdate().getBusinessRegisterPlace());
        institution.supportEmail(onboardingData.getInstitutionUpdate().getSupportEmail());
        institution.supportPhone(onboardingData.getInstitutionUpdate().getSupportPhone());
        institution.imported(onboardingData.getInstitutionUpdate().getImported());
        return institution;
    }


    @Named("toInstitutionPsp")
    default InstitutionPspRequest toInstitutionPsp(OnboardingData onboardingData) {
        InstitutionPspRequest institutionPsp = new InstitutionPspRequest();
        institutionPsp.institutionType(it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.InstitutionType.valueOf(onboardingData.getInstitutionType().name()));
        institutionPsp.taxCode(onboardingData.getTaxCode());
        institutionPsp.subunitCode(onboardingData.getSubunitCode());
        institutionPsp.subunitType(Optional.ofNullable(onboardingData.getSubunitType())
                .map(InstitutionPaSubunitType::valueOf)
                .orElse(null));

        institutionPsp.setOrigin(Optional.ofNullable(onboardingData.getOrigin()).map(Origin::fromValue).orElse(null));
        institutionPsp.setOriginId(onboardingData.getOriginId());
        if(Objects.nonNull(onboardingData.getLocation())) {
            institutionPsp.setCity(onboardingData.getLocation().getCity());
            institutionPsp.setCountry(onboardingData.getLocation().getCountry());
            institutionPsp.setCounty(onboardingData.getLocation().getCounty());
        }
        institutionPsp.setDescription(onboardingData.getInstitutionUpdate().getDescription());
        institutionPsp.digitalAddress(onboardingData.getInstitutionUpdate().getDigitalAddress());
        institutionPsp.address(onboardingData.getInstitutionUpdate().getAddress());
        institutionPsp.zipCode(onboardingData.getInstitutionUpdate().getZipCode());
        institutionPsp.geographicTaxonomies(Optional.ofNullable(onboardingData.getInstitutionUpdate().getGeographicTaxonomies())
                .map(geotaxes -> geotaxes.stream()
                    .map(this::toGeographicTaxonomyDto)
                    .collect(Collectors.toList()))
                .orElse(null));
        institutionPsp.rea(onboardingData.getInstitutionUpdate().getRea());
        institutionPsp.shareCapital(onboardingData.getInstitutionUpdate().getShareCapital());
        institutionPsp.businessRegisterPlace(onboardingData.getInstitutionUpdate().getBusinessRegisterPlace());
        institutionPsp.supportEmail(onboardingData.getInstitutionUpdate().getSupportEmail());
        institutionPsp.supportPhone(onboardingData.getInstitutionUpdate().getSupportPhone());
        institutionPsp.imported(onboardingData.getInstitutionUpdate().getImported());


        institutionPsp.setPaymentServiceProvider(toPaymentServiceProviderRequest(onboardingData.getInstitutionUpdate().getPaymentServiceProvider()));
        institutionPsp.setDataProtectionOfficer(toDataProtectionOfficerRequest(onboardingData.getInstitutionUpdate().getDataProtectionOfficer()));
        return institutionPsp;
    }

    PaymentServiceProviderRequest toPaymentServiceProviderRequest(PaymentServiceProvider paymentServiceProvider);
    DataProtectionOfficerRequest toDataProtectionOfficerRequest(DataProtectionOfficer dataProtectionOfficer);

    @Mapping(target = "institutionUpdate", source = "institution")
    OnboardingData toOnboardingData(it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.OnboardingGet onboardingGet);

    @Mapping(target = "createdAt", expression = "java(institutionResponse.getCreatedAt() != null ? institutionResponse.getCreatedAt().atOffset(java.time.ZoneOffset.UTC) : null)")
    @Mapping(target = "updatedAt", expression = "java(institutionResponse.getUpdatedAt() != null ? institutionResponse.getCreatedAt().atOffset(java.time.ZoneOffset.UTC) : null)")
    @Mapping(target = "onboarding", expression = "java(toOnboardingData(institutionResponse.getOnboarding()))")
    Institution toInstitution(it.pagopa.selfcare.core.generated.openapi.v1.dto.InstitutionResponse institutionResponse);

    @Named("toOnboardingData")
    default List<Onboarding> toOnboardingData(List<OnboardedProductResponse> onboarding) {
        return onboarding.stream().map(onboardedProductResponse -> {
            Onboarding onb = new Onboarding();
            onb.setProductId(onboardedProductResponse.getProductId());
            onb.setBilling(toBilling(onboardedProductResponse.getBilling()));
            return onb;
        }).toList();
    }

    Billing toBilling(it.pagopa.selfcare.core.generated.openapi.v1.dto.BillingResponse billing);
}
