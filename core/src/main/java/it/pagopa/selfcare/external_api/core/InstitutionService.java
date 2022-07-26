package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.external_api.model.institutions.InstitutionInfo;

import java.util.Collection;

public interface InstitutionService {

    Collection<InstitutionInfo> getInstitutions();

}
