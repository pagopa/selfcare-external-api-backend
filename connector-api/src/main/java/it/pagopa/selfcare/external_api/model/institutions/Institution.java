package it.pagopa.selfcare.external_api.model.institutions;

import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(of = "id")
public class Institution {

    private String id;
    private String externalId;
    private String originId;
    private String description;
    private String digitalAddress;
    private String address;
    private String zipCode;
    private String country;
    private String county;
    private String city;
    private String taxCode;
    private String origin;
    private InstitutionType institutionType;
    private List<Attribute> attributes;
    private List<GeographicTaxonomy> geographicTaxonomies;
    private String rea;
    private String shareCapital;
    private String businessRegisterPlace;
    private String supportEmail;
    private String supportPhone;
    private Boolean imported;
    private String subunitCode;
    private String subunitType;
    private String parentDescription;
    private String aooParentCode;
    private CompanyInformations companyInformations;
    private AssistanceContacts assistanceContacts;
}
