package it.pagopa.selfcare.external_api.connector.rest.client;

import it.pagopa.selfcare.external_api.connector.rest.model.pnpg.CreatePnPgInstitutionRequest;
import it.pagopa.selfcare.external_api.connector.rest.model.pnpg.InstitutionPnPgResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient(name = "${rest-client.ms-core.serviceCode}", url = "${rest-client.ms-core.base-url}")
public interface MsCoreRestClient {
    @PostMapping(value = "${rest-client.ms-core.createPnPgInstitution.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    InstitutionPnPgResponse createPnPgInstitution(@RequestBody CreatePnPgInstitutionRequest request);
}
