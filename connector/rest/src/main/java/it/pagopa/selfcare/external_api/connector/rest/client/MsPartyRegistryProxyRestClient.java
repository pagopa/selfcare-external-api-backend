package it.pagopa.selfcare.external_api.connector.rest.client;

import it.pagopa.selfcare.external_api.connector.rest.model.institution.InstitutionResource;
import it.pagopa.selfcare.external_api.connector.rest.model.institution.Origin;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient(name = "${rest-client.ms-party-registry-proxy.serviceCode}", url = "${rest-client.ms-party-registry-proxy.base-url}")
public interface MsPartyRegistryProxyRestClient {

    @GetMapping(value = "${rest-client.ms-party-registry-proxy.findInstitution}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    InstitutionResource findInstitution(@PathVariable(value = "id") String institutionExternalId,
                                        @RequestParam(value = "origin", required = false) Origin origin,
                                        @RequestParam(value = "categories", required = false) String categories);

}
