package it.pagopa.selfcare.external_api.web.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import it.pagopa.selfcare.commons.web.model.Problem;
import it.pagopa.selfcare.external_api.core.OnboardingService;
import it.pagopa.selfcare.external_api.model.onboarding.InstitutionType;
import it.pagopa.selfcare.external_api.web.model.mapper.OnboardingMapper;
import it.pagopa.selfcare.external_api.web.model.onboarding.OnboardingDto;
import it.pagopa.selfcare.external_api.web.model.onboarding.OnboardingImportDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.ValidationException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;

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

    @ApiResponses(value = {
            @ApiResponse(responseCode = "409",
                    description = "Conflict",
                    content = {
                            @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                                    schema = @Schema(implementation = Problem.class))
                    }),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden",
                    content = {
                            @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                                    schema = @Schema(implementation = Problem.class))
                    })
    })
    @PostMapping(value = "/{externalInstitutionId}")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "", notes = "${swagger.external_api.onboarding.api.onboardingOldContract}")
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

    @ApiResponses(value = {
            @ApiResponse(responseCode = "409",
                    description = "Conflict",
                    content = {
                            @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                                    schema = @Schema(implementation = Problem.class))
                    }),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden",
                    content = {
                            @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                                    schema = @Schema(implementation = Problem.class))
                    })
    })
    @PostMapping(value = "/{externalInstitutionId}/products/{productId}")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "", notes = "${swagger.external_api.onboarding.api.autoApprovalOnboarding}")
    public void autoApprovalOnboarding(@ApiParam("${swagger.external_api.institutions.model.externalId}")
                                       @PathVariable("externalInstitutionId")
                                       String externalInstitutionId,
                                       @ApiParam("${swagger.external_api.products.model.id}")
                                       @PathVariable("productId")
                                       String productId,
                                       @RequestBody
                                       @Valid
                                       OnboardingDto request) {
        log.trace("autoApprovalOnboarding start");
        log.debug("autoApprovalOnboarding institutionId = {}, productId = {}, request = {}", externalInstitutionId, productId, request);
        if (InstitutionType.PSP.equals(request.getInstitutionType()) && request.getPspData() == null) {
            throw new ValidationException("Field 'pspData' is required for PSP institution onboarding");
        }
        onboardingService.autoApprovalOnboarding(OnboardingMapper.toOnboardingData(externalInstitutionId, productId, request));
        log.trace("autoApprovalOnboarding end");
    }

    @ApiResponse(responseCode = "403",
            description = "Forbidden",
            content = {
                    @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            })
    @RequestMapping(method = HEAD, value = "/{externalInstitutionId}/products/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.external_api.onboarding.api.verifyOnboarding}")
    public void verifyOnboarding(@ApiParam("${swagger.external_api.institutions.model.externalId}")
                                 @PathVariable("externalInstitutionId")
                                 String externalInstitutionId,
                                 @ApiParam("${swagger.external_api.products.model.id}")
                                 @PathVariable("productId")
                                 String productId) {
        log.trace("verifyOnboarding start");
        log.debug("verifyOnboarding externalInstitutionId = {}, productId = {}", externalInstitutionId, productId);
        onboardingService.verifyOnboarding(externalInstitutionId, productId);
        log.trace("verifyOnboarding end");
    }

}
