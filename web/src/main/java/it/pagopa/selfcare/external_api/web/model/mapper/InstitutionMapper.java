package it.pagopa.selfcare.external_api.web.model.mapper;

import it.pagopa.selfcare.external_api.model.institutions.BusinessData;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.external_api.model.institutions.SupportContact;
import it.pagopa.selfcare.external_api.model.onboarding.DataProtectionOfficer;
import it.pagopa.selfcare.external_api.model.onboarding.PaymentServiceProvider;
import it.pagopa.selfcare.external_api.web.model.institutions.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InstitutionMapper {

    public static InstitutionResource toResource(InstitutionInfo model) {
        InstitutionResource resource = null;
        if (model != null) {
            resource = new InstitutionResource();
            if (model.getId() != null) {
                resource.setId(UUID.fromString(model.getId()));
            }
            resource.setDescription(model.getDescription());
            resource.setExternalId(model.getExternalId());
            resource.setAddress(model.getAddress());
            resource.setStatus(model.getStatus());
            resource.setDigitalAddress(model.getDigitalAddress());
            resource.setTaxCode(model.getTaxCode());
            resource.setZipCode(model.getZipCode());
            resource.setOrigin(model.getOrigin());
            resource.setOriginId(model.getOriginId());
            if (model.getProductRoles() != null)
                resource.setUserProductRoles(model.getProductRoles());
            if (model.getBilling() != null)
                resource.setRecipientCode(model.getBilling().getRecipientCode());
            resource.setCompanyInformations(toResource(model.getBusinessData()));
            resource.setAssistanceContacts(toResource(model.getSupportContact()));
            resource.setPspData(toResource(model.getPaymentServiceProvider()));
            resource.setDpoData(toResource(model.getDataProtectionOfficer()));
        }
        return resource;
    }

    public static InstitutionDetailResource toResource(Institution model) {
        InstitutionDetailResource resource = null;
        if (model != null) {
            resource = new InstitutionDetailResource();
            if (model.getId() != null) {
                resource.setId(UUID.fromString(model.getId()));
            }
            resource.setDescription(model.getDescription());
            resource.setExternalId(model.getExternalId());
            resource.setAddress(model.getAddress());
            resource.setDigitalAddress(model.getDigitalAddress());
            resource.setGeographicTaxonomies(model.getGeographicTaxonomies().stream()
                    .map(GeographicTaxonomyMapper::toResource)
                    .collect(Collectors.toList()));
            resource.setTaxCode(model.getTaxCode());
            resource.setZipCode(model.getZipCode());
            resource.setInstitutionType(model.getInstitutionType());
            resource.setOrigin(model.getOrigin());
            resource.setOriginId(model.getOriginId());
            resource.setRea(model.getRea());
            resource.setShareCapital(model.getShareCapital());
            resource.setBusinessRegisterPlace(model.getBusinessRegisterPlace());
            resource.setSupportEmail(model.getSupportEmail());
            resource.setSupportPhone(model.getSupportPhone());
            resource.setImported(model.getImported());
        }
        return resource;
    }


    public static AssistanceContactsResource toResource(SupportContact model) {
        AssistanceContactsResource resource = null;
        if (model != null) {
            resource = new AssistanceContactsResource();
            resource.setSupportEmail(model.getSupportEmail());
            resource.setSupportPhone(model.getSupportPhone());
        }
        return resource;
    }

    public static CompanyInformationsResource toResource(BusinessData model) {
        CompanyInformationsResource resource = null;
        if (model != null) {
            resource = new CompanyInformationsResource();
            resource.setRea(model.getRea());
            resource.setShareCapital(model.getShareCapital());
            resource.setBusinessRegisterPlace(model.getBusinessRegisterPlace());
        }
        return resource;
    }

    public static PspDataResource toResource(PaymentServiceProvider model) {
        PspDataResource resource = null;
        if (model != null) {
            resource = new PspDataResource();
            resource.setBusinessRegisterNumber(model.getBusinessRegisterNumber());
            resource.setLegalRegisterName(model.getLegalRegisterName());
            resource.setLegalRegisterNumber(model.getLegalRegisterNumber());
            resource.setAbiCode(model.getAbiCode());
            resource.setVatNumberGroup(model.getVatNumberGroup());
        }
        return resource;
    }

    public static DpoDataResource toResource(DataProtectionOfficer model) {
        DpoDataResource resource = null;
        if (model != null) {
            resource = new DpoDataResource();
            resource.setAddress(model.getAddress());
            resource.setPec(model.getPec());
            resource.setEmail(model.getEmail());
        }
        return resource;
    }
}
