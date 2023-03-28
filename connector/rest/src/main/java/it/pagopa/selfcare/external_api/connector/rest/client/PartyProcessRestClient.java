package it.pagopa.selfcare.external_api.connector.rest.client;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.external_api.connector.rest.model.institution.OnBoardingInfo;
import it.pagopa.selfcare.external_api.connector.rest.model.institution.RelationshipsResponse;
import it.pagopa.selfcare.external_api.connector.rest.model.onboarding.InstitutionSeed;
import it.pagopa.selfcare.external_api.connector.rest.model.onboarding.OnboardingImportInstitutionRequest;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.user.RelationshipState;
import org.springframework.cloud.openfeign.CollectionFormat;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.EnumSet;
import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;

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

    @RequestMapping(method = HEAD, value = "${rest-client.party-process.verifyOnboarding.path}")
    @ResponseBody
    ResponseEntity<Void> verifyOnboarding(@PathVariable("externalId") String externalInstitutionId,
                                          @PathVariable("productId") String productId);

    @GetMapping(value = "${rest-client.party-process.getInstitutionByExternalId.path}", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    Institution getInstitutionByExternalId(@PathVariable("externalId") String externalId);

    @PostMapping(value = "${rest-client.party-process.createInstitutionUsingExternalId.path}", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    Institution createInstitutionUsingExternalId(@PathVariable("externalId") String externalId);

    @PostMapping(value = "${rest-client.party-process.createInstitutionRaw.path}", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    Institution createInstitutionRaw(@PathVariable("externalId") String externalId,
                                     @RequestBody InstitutionSeed institutionSeed);

    @PostMapping(value = "${rest-client.party-process.onboardingOrganization.path}", consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    void onboardingOrganization(@RequestBody OnboardingImportInstitutionRequest request);

    @GetMapping(value = "${rest-client.party-process.getInstitution.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    Institution getInstitution(@PathVariable(value = "id") String id);

    @GetMapping(value = "${rest-client.party-process.getUserInstitutionRelationshipsByExternalId.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CollectionFormat(feign.CollectionFormat.CSV)
    RelationshipsResponse getUserInstitutionRelationshipsByExternalId(@PathVariable("externalId") String institutionId,
                                                                      @RequestParam(value = "personId", required = false) String personId,
                                                                      @RequestParam(value = "roles", required = false) EnumSet<PartyRole> roles,
                                                                      @RequestParam(value = "states", required = false) EnumSet<RelationshipState> states,
                                                                      @RequestParam(value = "products", required = false) Set<String> productIds,
                                                                      @RequestParam(value = "productRoles", required = false) Set<String> productRoles);
}
