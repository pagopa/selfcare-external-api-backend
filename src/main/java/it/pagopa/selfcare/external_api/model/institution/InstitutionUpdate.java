package it.pagopa.selfcare.external_api.model.institution;

import it.pagopa.selfcare.onboarding.common.InstitutionType;
import lombok.Data;

;

@Data
public class InstitutionUpdate {

    private InstitutionType institutionType;
    private String description;
    private String digitalAddress;
    private String address;
    private String taxCode;

}
