package it.pagopa.selfcare.external_api.web.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.external_api.core.OnboardingService;
import it.pagopa.selfcare.external_api.web.model.mapper.OnboardingMapper;
import it.pagopa.selfcare.external_api.web.model.onboarding.OnboardingImportDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping(value = "/onboarding", produces = APPLICATION_JSON_VALUE)
@Api(tags = "onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;

    @Autowired
    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }


    @PostMapping(value = "/{externalInstitutionId}")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "", notes = "${swagger.external_api.institutions.api.onboardingOldContract}")
    public void oldContractOnboarding(@ApiParam("${swagger.external_api.institutions.model.externalId}")
                                      @PathVariable("externalInstitutionId")
                                      String externalInstitutionId,
                                      @RequestBody
                                      @Valid
                                      OnboardingImportDto request) {
        log.trace("oldContractonboarding start");
        log.debug("oldContractonboarding institutionId = {}, request = {}", externalInstitutionId, request);
        onboardingService.oldContractOnboarding(OnboardingMapper.toOnboardingImportData(externalInstitutionId, request));
        log.trace("oldContractonboarding end");
    }

}
