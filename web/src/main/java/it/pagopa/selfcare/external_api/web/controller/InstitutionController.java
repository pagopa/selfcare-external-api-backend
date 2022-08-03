package it.pagopa.selfcare.external_api.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.external_api.core.InstitutionService;
import it.pagopa.selfcare.external_api.web.model.institutions.InstitutionResource;
import it.pagopa.selfcare.external_api.web.model.mapper.InstitutionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @GetMapping("")
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
}
