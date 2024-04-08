package it.pagopa.selfcare.external_api.connector.rest.mapper;

import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.OnboardedProductResponse;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.OnboardingResponse;
import it.pagopa.selfcare.external_api.connector.rest.model.institution.InstitutionResponse;
import it.pagopa.selfcare.external_api.model.institutions.*;
import it.pagopa.selfcare.external_api.model.onboarding.InstitutionOnboarding;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionInfo;
import it.pagopa.selfcare.external_api.model.onboarding.ProductInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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
    @Mapping(target = "businessData", source = ".", qualifiedByName = "toBusinessData")
    @Mapping(target = "supportContact", source = ".", qualifiedByName = "toSupportContact")
    OnboardedInstitutionInfo toOnboardedInstitution(it.pagopa.selfcare.core.generated.openapi.v1.dto.InstitutionResponse institutionResponse);

    @Mapping(target = "id", source = "productId")
    @Mapping(target = "status", expression = "java(onboardedProductResponse.getStatus().name())")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "toOffsetDateTime")
    ProductInfo toProductInfo(OnboardedProductResponse onboardedProductResponse);

    @Named("toBusinessData")
    static BusinessData toBusinessData(it.pagopa.selfcare.core.generated.openapi.v1.dto.InstitutionResponse institutionResponse) {
        BusinessData businessData = new BusinessData();
        businessData.setRea(institutionResponse.getRea());
        businessData.setBusinessRegisterPlace(institutionResponse.getBusinessRegisterPlace());
        businessData.setShareCapital(institutionResponse.getShareCapital());
        return businessData;
    }

    @Named("toSupportContact")
    static SupportContact toSupportContact(it.pagopa.selfcare.core.generated.openapi.v1.dto.InstitutionResponse institutionResponse) {
        SupportContact supportContact = new SupportContact();
        supportContact.setSupportEmail(institutionResponse.getSupportEmail());
        supportContact.setSupportPhone(institutionResponse.getSupportPhone());
        return supportContact;
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
}