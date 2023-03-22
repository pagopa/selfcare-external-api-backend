package it.pagopa.selfcare.external_api.api;

import it.pagopa.selfcare.external_api.model.institutions.InstitutionResource;

public interface MsPartyRegistryProxyConnector {

    InstitutionResource findInstitution(String institutionExternalId);

}
