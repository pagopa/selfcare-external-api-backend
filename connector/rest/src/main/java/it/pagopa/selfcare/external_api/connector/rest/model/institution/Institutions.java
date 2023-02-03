package it.pagopa.selfcare.external_api.connector.rest.model.institution;

import it.pagopa.selfcare.external_api.model.institutions.Institution;
import lombok.Data;

import java.util.List;

@Data
public class Institutions {

    private List<Institution> items;
}
