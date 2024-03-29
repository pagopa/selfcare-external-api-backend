package it.pagopa.selfcare.external_api.connector.rest.client;

import it.pagopa.selfcare.external_api.connector.rest.model.pnpg.CreatePnPgInstitutionRequest;
import it.pagopa.selfcare.external_api.connector.rest.model.pnpg.InstitutionPnPgResponse;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingInfoResponse;
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

    @GetMapping(value = "${rest-client.ms-core.onboardingInfo.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    OnboardingInfoResponse getInstitutionProductsInfo(@PathVariable(value = "userId") String userId);

    @GetMapping(value = "${rest-client.ms-core.onboardingInfo.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    OnboardingInfoResponse getInstitutionProductsInfo(@PathVariable(value = "userId") String userId,
                                                      @RequestParam(value="states") String[] states);
}
