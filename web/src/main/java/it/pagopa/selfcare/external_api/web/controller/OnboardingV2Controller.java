package it.pagopa.selfcare.external_api.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import it.pagopa.selfcare.commons.web.model.Problem;
import it.pagopa.selfcare.external_api.core.OnboardingService;
import it.pagopa.selfcare.external_api.web.model.mapper.OnboardingResourceMapper;
import it.pagopa.selfcare.external_api.web.model.onboarding.OnboardingProductDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping(value = "/v2/onboarding", produces = APPLICATION_JSON_VALUE)
@Api(tags = "onboarding")
public class OnboardingV2Controller {

    private final OnboardingService onboardingService;
    private final OnboardingResourceMapper onboardingResourceMapper;

    protected static final String LOCATION_INFO_IS_REQUIRED = "Field 'Location' is required";


    @Autowired
    public OnboardingV2Controller(OnboardingService onboardingService,
                                  OnboardingResourceMapper onboardingResourceMapper) {
        this.onboardingService = onboardingService;
        this.onboardingResourceMapper = onboardingResourceMapper;
    }

    @ApiResponse(responseCode = "403",
            description = "Forbidden",
            content = {
                    @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "", notes = "${swagger.onboarding.institutions.api.onboarding.subunit}")
    public void onboarding(@RequestBody @Valid OnboardingProductDto request) {
        log.trace("onboarding start");
        log.debug("onboarding request = {}", request);
        onboardingService.autoApprovalOnboardingProductV2(onboardingResourceMapper.toEntity(request));
        log.trace("onboarding end");
    }

}
