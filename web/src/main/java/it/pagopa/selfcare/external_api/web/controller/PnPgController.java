package it.pagopa.selfcare.external_api.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.external_api.core.InstitutionService;
import it.pagopa.selfcare.external_api.web.model.mapper.PnPgMapper;
import it.pagopa.selfcare.external_api.web.model.pnpg.CreatePnPgInstitutionDto;
import it.pagopa.selfcare.external_api.web.model.pnpg.PnPgInstitutionIdResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping(value = "/pn-pg", produces = APPLICATION_JSON_VALUE)
@Api(tags = "institutions-pnpg")
public class PnPgController {
    private final InstitutionService institutionService;

    @Autowired
    public PnPgController(InstitutionService institutionService) {
        this.institutionService = institutionService;
    }

    @PostMapping(value = "/institutions/add")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.external-api.institutions.api.addInstitution}")
    public PnPgInstitutionIdResource addInstitution(@ApiParam("swagger.external-api.institutions.model.searchInstitutionDto")
                                 @RequestBody
                                 @Valid
                                 CreatePnPgInstitutionDto createPnPgInstitutionDto) {
        log.trace("addInstitution start");
        log.debug("addInstitution searchInstitutionDto = {}", createPnPgInstitutionDto);
        String institutionId = institutionService.addInstitution(PnPgMapper.fromDto(createPnPgInstitutionDto));
        PnPgInstitutionIdResource id = new PnPgInstitutionIdResource(institutionId);
        log.debug("addInstitution result = {}", id);
        log.trace("addInstitution end");
        return id;
    }
}
