package it.pagopa.selfcare.external_api.connector.rest.model.institution;

import lombok.Data;

import java.util.List;

@Data
public class InstitutionsResponse {

    private List<InstitutionResponse> institutions;
}
