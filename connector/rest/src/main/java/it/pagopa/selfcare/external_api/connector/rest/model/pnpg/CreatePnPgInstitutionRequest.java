package it.pagopa.selfcare.external_api.connector.rest.model.pnpg;

import it.pagopa.selfcare.external_api.model.pnpg.CreatePnPgInstitution;
import lombok.Data;

@Data
public class CreatePnPgInstitutionRequest {

    public CreatePnPgInstitutionRequest(CreatePnPgInstitution institution) {
        this.taxId = institution.getExternalId();
        this.description = institution.getDescription();
    }

    private String taxId;
    private String description;
}
