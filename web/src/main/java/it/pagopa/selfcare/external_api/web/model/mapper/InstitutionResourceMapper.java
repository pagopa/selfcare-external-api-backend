package it.pagopa.selfcare.external_api.web.model.mapper;

import it.pagopa.selfcare.external_api.model.institutions.*;
import it.pagopa.selfcare.external_api.model.onboarding.*;
import it.pagopa.selfcare.external_api.web.model.institutions.InstitutionResource;
import it.pagopa.selfcare.external_api.web.model.institutions.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface InstitutionResourceMapper {

    @Mapping(target = "recipientCode", source = "model.billing.recipientCode")
    @Mapping(target = "taxCodeInvoicing", source = "model.billing.taxCodeInvoicing")
    @Mapping(target = "pspData", source = "model.paymentServiceProvider", qualifiedByName = "toPspDataResource")
    @Mapping(target = "companyInformations", source = "model.businessData", qualifiedByName = "toCompanyInformationResource")
    @Mapping(target = "assistanceContacts", source = "model.supportContact", qualifiedByName = "toAssistanceContactsResource")
    @Mapping(target = "dpoData", source = "model.dataProtectionOfficer", qualifiedByName = "toDpoDataResource")
    @Mapping(target = "rootParent", source = "model.rootParent", qualifiedByName = "toRootParentResource")
    @Mapping(target = "userProductRoles", source = "model.productRoles")
    @Mapping(target = "city", source = "institutionLocation.city")
    @Mapping(target = "county", source = "institutionLocation.county")
    @Mapping(target = "country", source = "institutionLocation.country")
    InstitutionResource toResource(InstitutionInfo model);

    @Mapping(target = "recipientCode", source = "model.billing.recipientCode")
    @Mapping(target = "taxCodeInvoicing", source = "model.billing.taxCodeInvoicing")
    @Mapping(target = "pspData", source = "paymentServiceProvider", qualifiedByName = "toPspDataResource")
    @Mapping(target = "companyInformations.rea", source = "rea")
    @Mapping(target = "companyInformations.shareCapital", source = "shareCapital")
    @Mapping(target = "companyInformations.businessRegisterPlace", source = "businessRegisterPlace")
    @Mapping(target = "assistanceContacts.supportPhone", source = "supportPhone")
    @Mapping(target = "assistanceContacts.supportEmail", source = "supportEmail")
    @Mapping(target = "status", source = "state")
    @Mapping(target = "dpoData", source = "dataProtectionOfficer", qualifiedByName = "toDpoDataResource")
    @Mapping(target = "rootParent", source = "rootParent", qualifiedByName = "toRootParentResource")
    @Mapping(target = "userProductRoles", source = "productInfo", qualifiedByName = "toUserProductRoles")
    InstitutionResource toResource(OnboardedInstitutionInfo model);

    @Mapping(target = "pspData", source = "paymentServiceProvider", qualifiedByName = "toPspDataResource")
    @Mapping(target = "companyInformations.rea", source = "rea")
    @Mapping(target = "companyInformations.shareCapital", source = "shareCapital")
    @Mapping(target = "companyInformations.businessRegisterPlace", source = "businessRegisterPlace")
    @Mapping(target = "assistanceContacts.supportPhone", source = "supportPhone")
    @Mapping(target = "assistanceContacts.supportEmail", source = "supportEmail")
    @Mapping(target = "dpoData", source = "dataProtectionOfficer", qualifiedByName = "toDpoDataResource")
    @Mapping(target = "rootParent", source = "rootParent", qualifiedByName = "toRootParentResource")
    InstitutionResource toResource(OnboardedInstitutionResource model);

    @Named("toUserProductRoles")
    static Collection<String> toUserProductRoles(ProductInfo productInfo) {
        Set<String> productRole = new HashSet<>();
        productRole.add(productInfo.getRole());
        return productRole;
    }

    @Mapping(target = "geographicTaxonomies", source = "model.geographicTaxonomies", qualifiedByName = "toGeographicTaxonomyResource")
    InstitutionDetailResource toResource(Institution model);

    @Named("toPspDataResource")
    PspDataResource toResource(PaymentServiceProvider model);

    @Named("toRootParentResource")
    RootParentResource toResource(RootParent model);

    @Named("toCompanyInformationResource")
    CompanyInformationsResource toResource(BusinessData model);

    @Named("toAssistanceContactsResource")
    AssistanceContactsResource toResource(SupportContact model);

    @Named("toDpoDataResource")
    DpoDataResource toResource(DataProtectionOfficer model);

    @Named("toGeographicTaxonomyResource")
    GeographicTaxonomyResource toResource(GeographicTaxonomy model);
}
