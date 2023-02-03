package it.pagopa.selfcare.external_api.connector.rest.client;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.external_api.connector.rest.model.institution.OnBoardingInfo;
import it.pagopa.selfcare.external_api.connector.rest.model.institution.RelationshipsResponse;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.user.RelationshipState;
import org.springframework.cloud.openfeign.CollectionFormat;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.EnumSet;
import java.util.Set;

@FeignClient(name = "${rest-client.party-process.serviceCode}", url = "${rest-client.party-process.base-url}")
public interface PartyProcessRestClient {

    @GetMapping(value = "${rest-client.party-process.getOnBoardingInfo.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CollectionFormat(feign.CollectionFormat.CSV)
    OnBoardingInfo getOnBoardingInfo(@RequestParam(value = "institutionExternalId", required = false) String institutionExternalId,
                                     @RequestParam(value = "states", required = false) EnumSet<RelationshipState> states);

    @GetMapping(value = "${rest-client.party-process.getUserInstitutionRelationships.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CollectionFormat(feign.CollectionFormat.CSV)
    RelationshipsResponse getUserInstitutionRelationships(@PathVariable("id") String institutionId,
                                                          @RequestParam(value = "roles", required = false) EnumSet<PartyRole> roles,
                                                          @RequestParam(value = "states", required = false) EnumSet<RelationshipState> states,
                                                          @RequestParam(value = "products", required = false) Set<String> productIds,
                                                          @RequestParam(value = "productRoles", required = false) Set<String> productRoles,
                                                          @RequestParam(value = "personId", required = false) String personId);

    @GetMapping(value = "${rest-client.party-process.getInstitution.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    Institution getInstitution(@PathVariable(value = "id") String id);
}
