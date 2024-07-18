package it.pagopa.selfcare.external_api.connector.rest.mapper;

import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.BillingResponse;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.OnboardedProductResponse;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.OnboardingResponse;
import it.pagopa.selfcare.external_api.connector.rest.model.institution.InstitutionResponse;
import it.pagopa.selfcare.external_api.model.institutions.AssistanceContacts;
import it.pagopa.selfcare.external_api.model.institutions.CompanyInformations;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.institutions.Onboarding;
import it.pagopa.selfcare.external_api.model.onboarding.Billing;
import it.pagopa.selfcare.external_api.model.onboarding.InstitutionOnboarding;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionInfo;
import it.pagopa.selfcare.external_api.model.onboarding.ProductInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Mapper(componentModel = "spring")
public interface InstitutionMapper {

    @Mapping(target = "companyInformations", source = ".", qualifiedByName = "toCompanyInformationsEntity")
    @Mapping(target = "assistanceContacts", source = ".", qualifiedByName = "toAssistanceContacts")
    Institution toEntity(InstitutionResponse dto);

    @Named("toCompanyInformationsEntity")
    static CompanyInformations toCompanyInformationsEntity(InstitutionResponse dto) {
        CompanyInformations companyInformations = new CompanyInformations();
        companyInformations.setRea(dto.getRea());
        companyInformations.setShareCapital(dto.getShareCapital());
        companyInformations.setBusinessRegisterPlace(dto.getBusinessRegisterPlace());
        return companyInformations;
    }

    @Named("toAssistanceContacts")
    static AssistanceContacts toAssistanceContacts(InstitutionResponse dto) {
        AssistanceContacts assistanceContacts = new AssistanceContacts();
        assistanceContacts.setSupportEmail(dto.getSupportEmail());
        assistanceContacts.setSupportPhone(dto.getSupportPhone());
        return assistanceContacts;
    }

    InstitutionOnboarding toEntity(OnboardingResponse response);

    @Mapping(target = "institutionType", source = "institutionType", qualifiedByName = "convertInstitutionType")
    @Mapping(target = "billing", source = "onboarding", qualifiedByName = "setBillingData")
    OnboardedInstitutionInfo toOnboardedInstitution(it.pagopa.selfcare.core.generated.openapi.v1.dto.InstitutionResponse institutionResponse);

    @Mapping(target = "id", source = "productId")
    @Mapping(target = "status", expression = "java(onboardedProductResponse.getStatus().name())")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "toOffsetDateTime")
    ProductInfo toProductInfo(OnboardedProductResponse onboardedProductResponse);

    @Named("setBillingData")
    static Billing setBillingData(List<OnboardedProductResponse> onboardings) {
        if(Objects.nonNull(onboardings) && !onboardings.isEmpty()) {
            Billing billing = null;
            BillingResponse billingResponse = onboardings.get(0).getBilling();
            if(Objects.nonNull(billingResponse)) {
                billing = new Billing();
                billing.setRecipientCode(billingResponse.getRecipientCode());
                billing.setVatNumber(billingResponse.getVatNumber());
                billing.setPublicServices(billingResponse.getPublicServices());
            }
            return billing;
        }
        return null;
    }

    @Named("convertInstitutionType")
    static InstitutionType convertInstitutionType(it.pagopa.selfcare.core.generated.openapi.v1.dto.InstitutionResponse.InstitutionTypeEnum institutionTypeEnum) {
        if(Objects.nonNull(institutionTypeEnum)) {
            String institutionType = institutionTypeEnum.name();
            return InstitutionType.valueOf(institutionType);
        }
        return null;
    }

    @Named("toOffsetDateTime")
    default OffsetDateTime map(LocalDateTime localDateTime) {
        return Optional.ofNullable(localDateTime)
                .map(time -> time.atZone(ZoneId.systemDefault()).toOffsetDateTime())
                .orElse(null);
    }

    @Mapping(target = "onboarding", source = "onboarding", qualifiedByName = "convertOnboading")
    Institution toInstitution(it.pagopa.selfcare.core.generated.openapi.v1.dto.InstitutionResponse body);

    @Named("convertOnboading")
    default Onboarding convertOnboarding(OnboardedProductResponse onboardedProductResponse) {
        Onboarding onboarding = toOnboarding(onboardedProductResponse);
        onboarding.setCreatedAt(convertDate(onboardedProductResponse.getCreatedAt()));
        onboarding.setUpdatedAt(convertDate(onboardedProductResponse.getUpdatedAt()));
        return onboarding;
    }

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "closedAt", ignore = true)
    Onboarding toOnboarding(OnboardedProductResponse onboardedProductResponse);

    default OffsetDateTime convertDate(LocalDateTime localDateTime) {
        return Optional.ofNullable(localDateTime)
                .map(time -> time.atZone(ZoneId.systemDefault()).toOffsetDateTime())
                .orElse(null);
    }
}