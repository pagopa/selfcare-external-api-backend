package it.pagopa.selfcare.external_api.api;

import it.pagopa.selfcare.external_api.model.pnpg.CreatePnPgInstitution;

public interface MsCoreConnector {
    String createPnPgInstitution(CreatePnPgInstitution request);
}
