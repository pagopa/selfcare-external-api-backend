package it.pagopa.selfcare.external_api.connector.rest.model.institution;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class InstitutionFromIpaPost {

    @NotNull
    private String taxCode;
    private String subunitCode;
    private String subunitType;
}
