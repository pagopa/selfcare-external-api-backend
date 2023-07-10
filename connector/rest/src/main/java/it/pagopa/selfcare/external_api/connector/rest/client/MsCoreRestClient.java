package it.pagopa.selfcare.external_api.connector.rest.client;

import it.pagopa.selfcare.external_api.connector.rest.model.pnpg.CreatePnPgInstitutionRequest;
import it.pagopa.selfcare.external_api.connector.rest.model.pnpg.InstitutionPnPgResponse;
import it.pagopa.selfcare.external_api.model.token.Token;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "${rest-client.ms-core.serviceCode}", url = "${rest-client.ms-core.base-url}")
public interface MsCoreRestClient {
    @PostMapping(value = "${rest-client.ms-core.createPnPgInstitution.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    InstitutionPnPgResponse createPnPgInstitution(@RequestBody CreatePnPgInstitutionRequest request);

    @GetMapping(value = "${rest-client.ms-core.getToken.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    Token getToken(@RequestParam(value = "institutionId")String institutionId,
                   @RequestParam(value = "productId")String productId);
}
