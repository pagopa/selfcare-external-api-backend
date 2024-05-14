package it.pagopa.selfcare.external_api.model.institutions;

import lombok.Data;

@Data
public class InstitutionResource {

    private String id;
    private String originId;
    private String o;
    private String ou;
    private String aoo;
    private String taxCode;
    private String taxCodeSfe;
    private String category;
    private String description;
    private String digitalAddress;
    private String address;
    private String zipCode;
    private Origin origin;
    private String country;
    private String county;
    private String city;

}
