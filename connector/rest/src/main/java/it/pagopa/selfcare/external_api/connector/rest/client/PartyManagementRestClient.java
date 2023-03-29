package it.pagopa.selfcare.external_api.connector.rest.client;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.external_api.connector.rest.model.institution.Institutions;
import it.pagopa.selfcare.external_api.model.institutions.SearchMode;
import it.pagopa.selfcare.external_api.model.relationship.Relationships;
import it.pagopa.selfcare.external_api.model.user.RelationshipState;
import org.springframework.cloud.openfeign.CollectionFormat;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.EnumSet;
import java.util.Set;

@FeignClient(name = "${rest-client.party-management.serviceCode}", url = "${rest-client.party-management.base-url}")
public interface PartyManagementRestClient {

    @GetMapping(value = "${rest-client.party-management.getRelationships.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CollectionFormat(feign.CollectionFormat.CSV)
    Relationships getRelationships(@RequestParam(value = "from", required = false) String from,
                                   @RequestParam(value = "to", required = false) String to,
                                   @RequestParam(value = "roles", required = false) EnumSet<PartyRole> roles,
                                   @RequestParam(value = "states", required = false) EnumSet<RelationshipState> states,
                                   @RequestParam(value = "products", required = false) Set<String> productIds,
                                   @RequestParam(value = "productRoles", required = false) Set<String> productRoles);

    @GetMapping(value = "${rest-client.party-management.findByGeoTaxonomies.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CollectionFormat(feign.CollectionFormat.CSV)
    Institutions getInstitutionsByGeoTaxonomies(@RequestParam(value = "geoTaxonomies") String geoTaxonomies,
                                                @RequestParam(value = "searchMode", required = false) SearchMode searchMode);
}
