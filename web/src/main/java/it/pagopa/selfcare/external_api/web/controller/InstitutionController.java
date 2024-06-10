package it.pagopa.selfcare.external_api.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import it.pagopa.selfcare.external_api.core.InstitutionService;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.institutions.SearchMode;
import it.pagopa.selfcare.external_api.web.model.institutions.GeographicTaxonomyResource;
import it.pagopa.selfcare.external_api.web.model.institutions.InstitutionDetailResource;
import it.pagopa.selfcare.external_api.web.model.mapper.GeographicTaxonomyMapper;
import it.pagopa.selfcare.external_api.web.model.mapper.InstitutionResourceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping(value = "/v1/institutions", produces = APPLICATION_JSON_VALUE)
@Api(tags = "Institution")
public class InstitutionController {

    private final InstitutionService institutionService;


    private final InstitutionResourceMapper institutionResourceMapper;


    @Autowired
    public InstitutionController(InstitutionService institutionService, InstitutionResourceMapper institutionResourceMapper) {
        this.institutionService = institutionService;
        this.institutionResourceMapper = institutionResourceMapper;
    }

    @Tag(name = "Institution")
    @GetMapping(value = "/{institutionId}/geographicTaxonomy")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.external-api.institutions.api.getInstitutionGeographicTaxonomy}")
    public List<GeographicTaxonomyResource> getInstitutionGeographicTaxonomies(@ApiParam("${swagger.external-api.institutions.model.id}")
                                                                               @PathVariable("institutionId")
                                                                               String institutionId) {
        log.trace("getInstitutionGeographicTaxonomy start");
        log.debug("getInstitutionGeographicTaxonomy institutionId = {}", institutionId);
        List<GeographicTaxonomyResource> geographicTaxonomies = institutionService.getGeographicTaxonomyList(institutionId)
                .stream()
                .map(GeographicTaxonomyMapper::toResource)
                .toList();
        log.debug("getInstitutionGeographicTaxonomy result = {}", geographicTaxonomies);
        log.trace("getInstitutionGeographicTaxonomy end");
        return geographicTaxonomies;
    }

    @Tag(name = "Institution")
    @GetMapping(value = "/byGeoTaxonomies")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.external-api.institutions.api.getInstitutionByGeoTaxonomies}")
    public List<InstitutionDetailResource> getInstitutionsByGeoTaxonomies(@ApiParam("${swagger.external-api.geographicTaxonomy.model.id}")
                                                                          @RequestParam("geoTaxonomies") Set<String> geoTaxonomies,
                                                                          @ApiParam("${swagger.external-api.geographicTaxonomy.searchMode}")
                                                                          @RequestParam(value = "searchMode", required = false) Optional<SearchMode> searchMode) {
        log.trace("getInstitutionByGeoTaxonomies start");
        log.debug("getInstitutionByGeoTaxonomies geoTaxonomies = {}, searchMode = {}", geoTaxonomies, searchMode);
        Collection<Institution> institutions = institutionService.getInstitutionsByGeoTaxonomies(geoTaxonomies, searchMode.orElse(null));
        List<InstitutionDetailResource> result = institutions.stream().map(institutionResourceMapper::toResource).toList();
        log.debug("getInstitutionByGeoTaxonomies result = {}", result);
        log.trace("getInstitutionByGeoTaxonomies end");
        return result;
    }
}
