package it.pagopa.selfcare.external_api.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import it.pagopa.selfcare.external_api.core.ContractService;
import it.pagopa.selfcare.external_api.core.InstitutionService;
import it.pagopa.selfcare.external_api.model.documents.ResourceResponse;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.institutions.SearchMode;
import it.pagopa.selfcare.external_api.model.user.UserInfo;
import it.pagopa.selfcare.external_api.web.model.institutions.GeographicTaxonomyResource;
import it.pagopa.selfcare.external_api.web.model.institutions.InstitutionDetailResource;
import it.pagopa.selfcare.external_api.web.model.institutions.InstitutionResource;
import it.pagopa.selfcare.external_api.web.model.mapper.GeographicTaxonomyMapper;
import it.pagopa.selfcare.external_api.web.model.mapper.InstitutionResourceMapper;
import it.pagopa.selfcare.external_api.web.model.mapper.ProductsMapper;
import it.pagopa.selfcare.external_api.web.model.mapper.UserMapper;
import it.pagopa.selfcare.external_api.web.model.products.ProductResource;
import it.pagopa.selfcare.external_api.web.model.user.UserResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

@Slf4j
@RestController
@RequestMapping(value = "/institutions", produces = APPLICATION_JSON_VALUE)
@Api(tags = "institutions")
public class InstitutionController {

    private final InstitutionService institutionService;

    private final ContractService contractService;

    private final InstitutionResourceMapper institutionResourceMapper;


    @Autowired
    public InstitutionController(InstitutionService institutionService, ContractService contractService, InstitutionResourceMapper institutionResourceMapper) {
        this.institutionService = institutionService;
        this.contractService = contractService;
        this.institutionResourceMapper = institutionResourceMapper;
    }

    @Tag(name = "external-v2")
    @GetMapping(value = "")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.external_api.institutions.api.getInstitutions}")
    public List<InstitutionResource> getInstitutions(@ApiParam("${swagger.external_api.products.model.id}")
                                                     @RequestParam(value = "productId") String productId) {
        log.trace("getInstitutions start");
        log.debug("getInstitutions productId = {}", productId);
        List<InstitutionResource> institutionResources = institutionService.getInstitutions(productId)
                .stream()
                .map(institutionResourceMapper::toResource)
                .collect(Collectors.toList());
        log.debug("getInstitutions result = {}", institutionResources);
        log.trace("getInstitutions end");
        return institutionResources;
    }

    @Tag(name = "external-v2")
    @GetMapping(value = "/{institutionId}/products")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.external_api.institutions.api.getInstitutionUserProducts}")
    public List<ProductResource> getInstitutionUserProducts(@ApiParam("${swagger.external_api.institutions.model.id}")
                                                            @PathVariable("institutionId") String institutionId) {
        log.trace("getInstitutionUserProducts start");
        log.debug("getInstitutionUserProducts institutionId = {}", institutionId);
        List<ProductResource> productResources = institutionService.getInstitutionUserProducts(institutionId)
                .stream()
                .map(ProductsMapper::toResource)
                .collect(Collectors.toList());
        log.debug("getInstitutionUserProducts result = {}", productResources);
        log.trace("getInstitutionUserProducts end");
        return productResources;
    }

    @Tag(name = "external-v2")
    @GetMapping(value = "/{institutionId}/products/{productId}/users")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.external_api.institutions.api.getInstitutionProductUsers}")
    public List<UserResource> getInstitutionProductsUsers(@ApiParam("${swagger.external_api.institutions.model.id}")
                                                          @PathVariable("institutionId") String institutionId,
                                                          @ApiParam("${swagger.external_api.products.model.id}")
                                                          @PathVariable("productId")
                                                          String productId,
                                                          @ApiParam("${swagger.external_api.user.model.id}")
                                                          @RequestParam(value = "userId", required = false)
                                                          Optional<String> userId,
                                                          @ApiParam("${swagger.external_api.model.productRoles}")
                                                          @RequestParam(value = "productRoles", required = false)
                                                          Optional<Set<String>> productRoles,
                                                          @RequestHeader(value = "x-selfcare-uid", required = false) Optional<String> xSelfCareUid) {
        log.trace("getInstitutionProductUsers start");
        log.debug("getInstitutionProductUsers institutionId = {}, productId = {}, productRoles = {}, xSelfCareUid = {}", institutionId, productId, productRoles, xSelfCareUid);
        Collection<UserInfo> userInfos = institutionService.getInstitutionProductUsers(institutionId, productId, userId, productRoles, xSelfCareUid.orElse(null));
        List<UserResource> result = userInfos.stream()
                .map(model -> UserMapper.toUserResource(model, productId))
                .collect(Collectors.toList());
        log.debug("getInstitutionProductUsers result = {}", result);
        log.trace("getInstitutionProductUsers end");
        return result;
    }

    @Tag(name = "external-v2")
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
                .collect(Collectors.toList());
        log.debug("getInstitutionGeographicTaxonomy result = {}", geographicTaxonomies);
        log.trace("getInstitutionGeographicTaxonomy end");
        return geographicTaxonomies;
    }

    @Tag(name = "external-v2")
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
        List<InstitutionDetailResource> result = institutions.stream().map(institutionResourceMapper::toResource).collect(Collectors.toList());
        log.debug("getInstitutionByGeoTaxonomies result = {}", result);
        log.trace("getInstitutionByGeoTaxonomies end");
        return result;
    }

    @Tags({@Tag(name = "external-v2"), @Tag(name = "support")})
    @GetMapping(value = "/{institutionId}/contract", produces = APPLICATION_OCTET_STREAM_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.external_api.documents.api.getContract}")
    public ResponseEntity<byte[]> getContract(@ApiParam("${swagger.external_api.institutions.model.id}")
                                              @PathVariable("institutionId")String institutionId,
                                              @ApiParam("${swagger.external_api.products.model.id}")
                                              @RequestParam(value = "productId") String productId){
        log.trace("getContract start");
        log.debug("getContract institutionId = {}, productId = {}", institutionId, productId);
        ResourceResponse contract = contractService.getContract(institutionId, productId);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, APPLICATION_OCTET_STREAM_VALUE);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + contract.getFileName());
        log.info("contentType: {}", headers.getContentType());
        log.debug("getContract result = {}", contract);
        log.trace("getContract end");
        return ResponseEntity.ok().headers(headers).body(contract.getData());
    }

}
