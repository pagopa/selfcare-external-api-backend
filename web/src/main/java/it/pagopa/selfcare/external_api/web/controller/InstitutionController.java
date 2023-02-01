package it.pagopa.selfcare.external_api.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.external_api.core.InstitutionService;
import it.pagopa.selfcare.external_api.model.user.UserInfo;
import it.pagopa.selfcare.external_api.web.model.institutions.GeographicTaxonomyResource;
import it.pagopa.selfcare.external_api.web.model.institutions.InstitutionResource;
import it.pagopa.selfcare.external_api.web.model.mapper.GeographicTaxonomyMapper;
import it.pagopa.selfcare.external_api.web.model.mapper.InstitutionMapper;
import it.pagopa.selfcare.external_api.web.model.mapper.ProductsMapper;
import it.pagopa.selfcare.external_api.web.model.mapper.UserMapper;
import it.pagopa.selfcare.external_api.web.model.products.ProductResource;
import it.pagopa.selfcare.external_api.web.model.user.UserResource;
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
@RequestMapping(value = "/institutions", produces = APPLICATION_JSON_VALUE)
@Api(tags = "institutions")
public class InstitutionController {

    private final InstitutionService institutionService;


    @Autowired
    public InstitutionController(InstitutionService institutionService) {
        this.institutionService = institutionService;
    }

    @GetMapping(value = "")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.external_api.institutions.api.getInstitutions}")
    public List<InstitutionResource> getInstitutions(@ApiParam("${swagger.external_api.products.model.id}")
                                                     @RequestParam(value = "productId") String productId) {
        log.trace("getInstitutions start");
        log.debug("getInstitutions productId = {}", productId);
        List<InstitutionResource> institutionResources = institutionService.getInstitutions(productId)
                .stream()
                .map(InstitutionMapper::toResource)
                .collect(Collectors.toList());
        log.debug("getInstitutions result = {}", institutionResources);
        log.trace("getInstitutions end");
        return institutionResources;
    }

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
                                                                  Optional<Set<String>> productRoles) {
        log.trace("getInstitutionProductUsers start");
        log.debug("getInstitutionProductUsers institutionId = {}, productId = {}, productRoles = {}", institutionId, productId, productRoles);
        Collection<UserInfo> userInfos = institutionService.getInstitutionProductUsers(institutionId, productId, userId, productRoles);
        List<UserResource> result = userInfos.stream()
                .map(model -> UserMapper.toUserResource(model, productId))
                .collect(Collectors.toList());
        log.debug("getInstitutionProductUsers result = {}", result);
        log.trace("getInstitutionProductUsers end");
        return result;
    }

    @GetMapping(value = "/{institutionId}/geographicTaxonomy")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.getInstitutionGeographicTaxonomy}")
    public List<GeographicTaxonomyResource> getInstitutionGeographicTaxonomies(@ApiParam("${swagger.dashboard.institutions.model.id}")
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


}
