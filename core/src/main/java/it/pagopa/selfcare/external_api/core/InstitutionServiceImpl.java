package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.external_api.api.PartyConnector;
import it.pagopa.selfcare.external_api.model.institutions.InstitutionInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@Slf4j
class InstitutionServiceImpl implements InstitutionService {

    private final PartyConnector partyConnector;

    @Autowired
    InstitutionServiceImpl(PartyConnector partyConnector) {
        this.partyConnector = partyConnector;
    }

    @Override
    public Collection<InstitutionInfo> getInstitutions(String productId) {
        log.trace("getInstitutions start");
        Collection<InstitutionInfo> result = partyConnector.getOnBoardedInstitutions(productId);
        log.debug("getInstitutions result = {}", result);
        log.trace("getInstitutions end");
        return result;
    }
}
