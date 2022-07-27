package it.pagopa.selfcare.external_api.api;

import it.pagopa.selfcare.external_api.model.institutions.InstitutionInfo;

import java.util.Collection;

public interface PartyConnector {

    Collection<InstitutionInfo> getOnBoardedInstitutions();

}
