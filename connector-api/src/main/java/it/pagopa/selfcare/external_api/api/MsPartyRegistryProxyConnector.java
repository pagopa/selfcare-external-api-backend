package it.pagopa.selfcare.external_api.api;

import it.pagopa.selfcare.external_api.model.institutions.InstitutionResource;
import it.pagopa.selfcare.external_api.model.nationalRegistries.LegalVerification;

public interface MsPartyRegistryProxyConnector {

    InstitutionResource findInstitution(String institutionExternalId);

    LegalVerification verifyLegal(String taxId, String vatNumber);

}
