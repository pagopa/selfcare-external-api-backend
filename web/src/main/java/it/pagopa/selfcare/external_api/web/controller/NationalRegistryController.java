package it.pagopa.selfcare.external_api.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import it.pagopa.selfcare.external_api.core.InstitutionService;
import it.pagopa.selfcare.external_api.model.nationalRegistries.LegalVerification;
import it.pagopa.selfcare.external_api.web.model.mapper.NationalRegistryMapper;
import it.pagopa.selfcare.external_api.web.model.national_registries.LegalVerificationResource;
import it.pagopa.selfcare.external_api.web.model.national_registries.VerifyRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@Api(tags = "national-registries")
@RequestMapping(value = "/v2/national-registries", produces = MediaType.APPLICATION_JSON_VALUE)
public class NationalRegistryController {
    private final InstitutionService institutionService;
    private final NationalRegistryMapper nationalRegistryMapper;

    public NationalRegistryController(InstitutionService institutionService, NationalRegistryMapper nationalRegistryMapper) {
        this.institutionService = institutionService;
        this.nationalRegistryMapper = nationalRegistryMapper;
    }

    @Tags({@Tag(name = "external-v2"), @Tag(name = "national-registries")})
    @PostMapping(value = "/legal-tax/verification", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.external-api.national-registries.api.verifyLegal}", nickname = "verifyLegalByPOST")
    public LegalVerificationResource verifyLegal(@RequestBody @Valid VerifyRequestDto requestDto){
        log.trace("verifyLegal start");
        LegalVerification legalVerification = institutionService.verifyLegal(requestDto.getTaxId(), requestDto.getVatNumber());
        log.trace("verifyLegal end");
        return nationalRegistryMapper.toResource(legalVerification);
    }
}
