package it.pagopa.selfcare.external_api.connector.rest.client;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.external_api.connector.rest.model.institution.*;
import it.pagopa.selfcare.external_api.connector.rest.model.onboarding.InstitutionSeed;
import it.pagopa.selfcare.external_api.connector.rest.model.onboarding.OnboardingImportInstitutionRequest;
import it.pagopa.selfcare.external_api.connector.rest.model.pnpg.CreatePnPgInstitutionRequest;
import it.pagopa.selfcare.external_api.connector.rest.model.pnpg.InstitutionPnPgResponse;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingInfoResponse;
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

@FeignClient(name = "${rest-client.ms-core.serviceCode}", url = "${rest-client.ms-core.base-url}")
public interface MsCoreRestClient {
    @PostMapping(value = "${rest-client.ms-core.createPnPgInstitution.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    InstitutionPnPgResponse createPnPgInstitution(@RequestBody CreatePnPgInstitutionRequest request);


    @GetMapping(value = "${rest-client.ms-core.onboardingInfo.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    OnboardingInfoResponse getInstitutionProductsInfo(@PathVariable(value = "userId") String userId);

    @GetMapping(value = "${rest-client.ms-core.onboardingInfo.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    OnboardingInfoResponse getInstitutionProductsInfo(@PathVariable(value = "userId") String userId,
                                                      @RequestParam(value="states") String[] states);

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

    @RequestMapping(method = HEAD, value = "${rest-client.party-process.verifyOnboardingByExternalId.path}")
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

    @PostMapping(value = "${rest-client.party-process.createInstitutionFromAnac.path}", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    InstitutionResponse createInstitutionFromANAC(@RequestBody InstitutionSeed institutionSeed);

    @PostMapping(value = "${rest-client.party-process.createInstitutionFromIvass.path}", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    InstitutionResponse createInstitutionFromIVASS(@RequestBody InstitutionSeed institutionSeed);
}
