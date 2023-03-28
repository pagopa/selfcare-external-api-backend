package it.pagopa.selfcare.external_api.model.institutions;

import it.pagopa.selfcare.external_api.model.onboarding.InstitutionType;
import lombok.Data;

@Data
public class InstitutionUpdate {

    private InstitutionType institutionType;
    private String description;
    private String digitalAddress;
    private String address;
    private String taxCode;

}
