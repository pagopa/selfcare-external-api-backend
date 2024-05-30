package it.pagopa.selfcare.external_api.connector.rest;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.external_api.api.MsPartyRegistryProxyConnector;
import it.pagopa.selfcare.external_api.connector.rest.client.MsPartyRegistryProxyRestClient;
import it.pagopa.selfcare.external_api.connector.rest.client.MsRegistryProxyNationalRegistryRestClient;
import it.pagopa.selfcare.external_api.connector.rest.mapper.RegistryProxyMapper;
import it.pagopa.selfcare.external_api.model.institutions.InstitutionResource;
import it.pagopa.selfcare.external_api.model.nationalRegistries.LegalVerification;
import it.pagopa.selfcare.registry_proxy.generated.openapi.v1.dto.LegalVerificationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
@Slf4j
public class MsPartyRegistryProxyConnectorImpl implements MsPartyRegistryProxyConnector {

    private final MsPartyRegistryProxyRestClient restClient;

    private final MsRegistryProxyNationalRegistryRestClient nationalRegistryRestClient;

    private final RegistryProxyMapper registryProxyMapper;

    protected static final String EXTERNAL_INSTITUTION_ID_IS_REQUIRED = "An external institution Id is required ";

    @Autowired
    public MsPartyRegistryProxyConnectorImpl(MsPartyRegistryProxyRestClient restClient, MsRegistryProxyNationalRegistryRestClient nationalRegistryRestClient, RegistryProxyMapper registryProxyMapper) {
        this.restClient = restClient;
        this.nationalRegistryRestClient = nationalRegistryRestClient;
        this.registryProxyMapper = registryProxyMapper;
    }

    @Override
    public InstitutionResource findInstitution(String institutionExternalId) {
        log.trace("getInstitutionCategory start");
        log.debug("getInstitutionCategory institutionExternalId = {}", institutionExternalId);
        Assert.hasText(institutionExternalId, EXTERNAL_INSTITUTION_ID_IS_REQUIRED);
        InstitutionResource result = restClient.findInstitution(institutionExternalId, null, null);
        log.trace("getInstitutionCategory institutionCategory retrieved = {}", result);
        log.debug("getInstitutionCategory end");
        return result;
    }

    @Override
    public LegalVerification verifyLegal(String taxId, String vatNumber){
        log.trace("verifyLegal start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "verifyLegal vatNumber = {}, taxId = {}", vatNumber, taxId);
        ResponseEntity<LegalVerificationResult> legalVerificationResultResponseEntity = nationalRegistryRestClient._verifyLegalUsingGET(taxId, vatNumber);
        log.trace("verifyLegal end");
        return registryProxyMapper.toLegalVerification(legalVerificationResultResponseEntity.getBody());
    }

}
