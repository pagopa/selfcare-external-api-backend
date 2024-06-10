package it.pagopa.selfcare.external_api.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.external_api.core.ContractService;
import it.pagopa.selfcare.external_api.core.InstitutionService;
import it.pagopa.selfcare.external_api.core.UserService;
import it.pagopa.selfcare.external_api.model.documents.ResourceResponse;
import it.pagopa.selfcare.external_api.model.user.RelationshipState;
import it.pagopa.selfcare.external_api.model.user.UserInfo;
import it.pagopa.selfcare.external_api.web.model.institutions.InstitutionResource;
import it.pagopa.selfcare.external_api.web.model.mapper.InstitutionResourceMapper;
import it.pagopa.selfcare.external_api.web.model.mapper.ProductsMapper;
import it.pagopa.selfcare.external_api.web.model.mapper.UserMapper;
import it.pagopa.selfcare.external_api.web.model.products.ProductResource;
import it.pagopa.selfcare.external_api.web.model.user.UserResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toCollection;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

@Slf4j
@RestController
@RequestMapping(value = "/v2/institutions", produces = APPLICATION_JSON_VALUE)
@Api(tags = "Institution")
public class InstitutionV2Controller {

    private final ContractService contractService;
    private final InstitutionService institutionService;
    private final UserService userService;
    private final ProductsMapper productsMapper;
    private final InstitutionResourceMapper institutionResourceMapper;

    public InstitutionV2Controller(ContractService contractService,
                                   InstitutionService institutionService,
                                   UserService userService,
                                   ProductsMapper productsMapper, InstitutionResourceMapper institutionResourceMapper) {
        this.contractService = contractService;
        this.institutionService = institutionService;
        this.productsMapper = productsMapper;
        this.institutionResourceMapper = institutionResourceMapper;
        this.userService = userService;
    }

    @Tag(name = "Institution")
    @GetMapping(value = "")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.external_api.institutions.api.getInstitutions}")
    public List<InstitutionResource> getInstitutions(@ApiParam("${swagger.external_api.products.model.id}")
                                                     @RequestParam(value = "productId") String productId,
                                                     Authentication authentication) {
        log.trace("getInstitutions start");
        SelfCareUser user = (SelfCareUser) authentication.getPrincipal();
        List<InstitutionResource> institutionResources = userService.getOnboardedInstitutionsDetailsActive(user.getId(), productId)
                .stream()
                .map(institutionResourceMapper::toResource)
                .filter(item -> RelationshipState.ACTIVE.name().equals(item.getStatus()))
                .collect(Collectors.collectingAndThen(toCollection(() -> new TreeSet<>(comparing(InstitutionResource::getId))),
                        ArrayList::new));
        log.debug("getInstitutions result = {}", institutionResources);
        log.trace("getInstitutions end");
        return institutionResources;
    }

    @Tag(name = "external-v2")
    @Tag(name = "support")
    @Tag(name = "Institution")
    @GetMapping(value = "/{institutionId}/contract", produces = APPLICATION_OCTET_STREAM_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.external_api.documents.api.getContract}")
    public ResponseEntity<byte[]> getContract(@ApiParam("${swagger.external_api.institutions.model.id}")
                                              @PathVariable("institutionId") String institutionId,
                                              @ApiParam("${swagger.external_api.products.model.id}")
                                              @RequestParam(value = "productId") String productId){
        log.trace("getContract start");
        log.debug("getContract institutionId = {}, productId = {}", institutionId, productId);
        ResourceResponse contract = contractService.getContractV2(institutionId, productId);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, APPLICATION_OCTET_STREAM_VALUE);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + contract.getFileName());
        log.info("contentType: {}", headers.getContentType());
        log.debug("getContract result = {}", contract);
        log.trace("getContract end");
        return ResponseEntity.ok().headers(headers).body(contract.getData());
    }

    @Tag(name = "external-v2")
    @Tag(name = "Institution")
    @GetMapping(value = "/{institutionId}/products")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.external_api.institutions.api.getInstitutionUserProducts}")
    public List<ProductResource> getInstitutionProducts(@ApiParam("${swagger.external_api.institutions.model.id}")
                                                            @PathVariable("institutionId") String institutionId,
                                                          @ApiParam("${swagger.external_api.user.model.id}")
                                                          @RequestParam(value = "userId") String userId) {
        log.trace("getInstitutionUserProducts start");
        List<ProductResource> productResources = institutionService.getInstitutionUserProductsV2(institutionId, userId)
                .stream()
                .map(productsMapper::toResource)
                .toList();
        log.debug("getInstitutionUserProducts result = {}", productResources);
        log.trace("getInstitutionUserProducts end");
        return productResources;
    }

    @Tag(name = "external-v2")
    @Tag(name = "Institution")
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
         Collection<UserInfo> userInfos = institutionService.getInstitutionProductUsersV2(institutionId, productId, userId.orElse(null), productRoles, xSelfCareUid.orElse(null));
        List<UserResource> result = userInfos.stream()
                .map(model -> UserMapper.toUserResource(model, productId))
                .toList();
        log.debug("getInstitutionProductUsers result = {}", result);
        log.trace("getInstitutionProductUsers end");
        return result;
    }
}
