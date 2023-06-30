package it.pagopa.selfcare.external_api.web.model.mapper;

import it.pagopa.selfcare.external_api.model.institutions.*;
import it.pagopa.selfcare.external_api.model.onboarding.DataProtectionOfficer;
import it.pagopa.selfcare.external_api.model.onboarding.PaymentServiceProvider;
import it.pagopa.selfcare.external_api.web.model.institutions.InstitutionResource;
import it.pagopa.selfcare.external_api.web.model.institutions.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface InstitutionMapper {

    @Mapping(target =  "recipientCode", source = "model.billing.recipientCode")
    @Mapping(target = "pspData", source = "model.paymentServiceProvider", qualifiedByName = "toPspDataResource")
    @Mapping(target = "companyInformations", source = "model.businessData", qualifiedByName = "toCompanyInformationResource")
    @Mapping(target = "assistanceContacts", source = "model.supportContact", qualifiedByName = "toAssistanceContactsResource")
    @Mapping(target = "dpoData", source = "model.dataProtectionOfficer", qualifiedByName = "toDpoDataResource")
    @Mapping(target = "userProductRoles", source = "model.productRoles")
    InstitutionResource toResource(InstitutionInfo model);

    @Mapping(target = "geographicTaxonomies", source = "model.geographicTaxonomies", qualifiedByName = "toGeographicTaxonomyResource")
    InstitutionDetailResource toResource(Institution model);
    @Named("toPspDataResource")
    PspDataResource toResource(PaymentServiceProvider model);

    @Named("toCompanyInformationResource")
    CompanyInformationsResource toResource(BusinessData model);

    @Named("toAssistanceContactsResource")
    AssistanceContactsResource toResource(SupportContact model);

    @Named("toDpoDataResource")
    DpoDataResource toResource(DataProtectionOfficer model);

    @Named("toGeographicTaxonomyResource")
    GeographicTaxonomyResource toResource(GeographicTaxonomy model);
}
