package it.pagopa.selfcare.external_api.connector.rest;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.external_api.api.MsCoreConnector;
import it.pagopa.selfcare.external_api.connector.rest.client.MsCoreRestClient;
import it.pagopa.selfcare.external_api.connector.rest.model.pnpg.CreatePnPgInstitutionRequest;
import it.pagopa.selfcare.external_api.connector.rest.model.pnpg.InstitutionPnPgResponse;
import it.pagopa.selfcare.external_api.model.pnpg.CreatePnPgInstitution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MsCoreConnectorImpl implements MsCoreConnector {
    private final MsCoreRestClient restClient;

    @Autowired
    public MsCoreConnectorImpl(MsCoreRestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public String createPnPgInstitution(CreatePnPgInstitution request) {
        log.trace("createPnPgInstitution start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "createPnPgInstitution request = {}", request);
        CreatePnPgInstitutionRequest institutionRequest = new CreatePnPgInstitutionRequest(request);
        InstitutionPnPgResponse pnPgInstitution = restClient.createPnPgInstitution(institutionRequest);
        log.debug("createPnPgInstitution result = {}", pnPgInstitution.getId());
        log.trace("createPnPgInstitution end");
        return pnPgInstitution.getId();
    }
}
