package it.pagopa.selfcare.external_api.connector.rest;

import it.pagopa.selfcare.external_api.api.MsPartyRegistryProxyConnector;
import it.pagopa.selfcare.external_api.connector.rest.client.MsPartyRegistryProxyRestClient;
import it.pagopa.selfcare.external_api.connector.rest.model.institution.InstitutionResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
@Slf4j
public class MsPartyRegistryProxyConnectorImpl implements MsPartyRegistryProxyConnector {

    private final MsPartyRegistryProxyRestClient restClient;

    protected static final String EXTERNAL_INSTITUTION_ID_IS_REQUIRED = "An external institution Id is required ";

    @Autowired
    public MsPartyRegistryProxyConnectorImpl(MsPartyRegistryProxyRestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public String getInstitutionCategory(String institutionExternalId) {
        log.trace("getInstitutionCategory start");
        log.debug("getInstitutionCategory institutionExternalId = {}", institutionExternalId);
        Assert.hasText(institutionExternalId, EXTERNAL_INSTITUTION_ID_IS_REQUIRED);
        InstitutionResource result = restClient.findInstitution(institutionExternalId, null, null);
        String institutionCategory = result.getCategory();
        log.trace("getInstitutionCategory institutionCategory retrieved = {}", institutionCategory);
        log.debug("getInstitutionCategory end");
        return institutionCategory;
    }

}
