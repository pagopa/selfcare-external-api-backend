package it.pagopa.selfcare.external_api.connector.rest.client;

import it.pagopa.selfcare.external_api.connector.rest.model.OnBoardingInfo;
import it.pagopa.selfcare.external_api.model.onboarding.RelationshipState;
import org.springframework.cloud.openfeign.CollectionFormat;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.EnumSet;

@FeignClient(name = "${rest-client.party-process.serviceCode}", url = "${rest-client.party-process.base-url}")
public interface PartyProcessRestClient {

    @GetMapping(value = "${rest-client.party-process.getOnBoardingInfo.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CollectionFormat(feign.CollectionFormat.CSV)
    OnBoardingInfo getOnBoardingInfo(@RequestParam(value = "institutionExternalId", required = false) String institutionExternalId,
                                     @RequestParam(value = "states", required = false) EnumSet<RelationshipState> states);
}
