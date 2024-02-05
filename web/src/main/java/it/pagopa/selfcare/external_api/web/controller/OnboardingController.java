package it.pagopa.selfcare.external_api.web.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import it.pagopa.selfcare.commons.base.utils.Origin;
import it.pagopa.selfcare.commons.web.model.Problem;
import it.pagopa.selfcare.external_api.core.OnboardingService;
import it.pagopa.selfcare.external_api.web.model.mapper.OnboardingMapper;
import it.pagopa.selfcare.external_api.web.model.mapper.OnboardingResourceMapper;
import it.pagopa.selfcare.external_api.web.model.onboarding.OnboardingDto;
import it.pagopa.selfcare.external_api.web.model.onboarding.OnboardingImportDto;
import it.pagopa.selfcare.external_api.web.model.onboarding.OnboardingProductDto;
import it.pagopa.selfcare.external_api.web.model.onboarding.PdaOnboardingDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.time.OffsetDateTime;
import java.util.Objects;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;


@Slf4j
@RestController
@RequestMapping(value = "/v1/onboarding", produces = APPLICATION_JSON_VALUE)
@Api(tags = "onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;
    private final OnboardingResourceMapper onboardingResourceMapper;

    protected static final String LOCATION_INFO_IS_REQUIRED = "Field 'Location' is required";


    @Autowired
    public OnboardingController(OnboardingService onboardingService,
                                OnboardingResourceMapper onboardingResourceMapper) {
        this.onboardingService = onboardingService;
        this.onboardingResourceMapper = onboardingResourceMapper;
    }

    @Deprecated(forRemoval = true)
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
        if (request.getImportContract().getOnboardingDate().compareTo(OffsetDateTime.now()) > 0) {
            throw new ValidationException("Invalid onboarding date: the onboarding date must be prior to the current date.");
        }
        onboardingService.oldContractOnboarding(OnboardingMapper.toOnboardingImportData(externalInstitutionId, request));
        log.trace("oldContractonboarding end");
    }

    @Deprecated(forRemoval = true)
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
        if (InstitutionType.PSP.equals(request.getInstitutionType()) && request.getPspData() == null) {
            throw new ValidationException("Field 'pspData' is required for PSP institution onboarding");
        } else if (!InstitutionType.SA.equals(request.getInstitutionType()) && Objects.isNull(request.getBillingData().getRecipientCode())){
            throw new ValidationException("Field 'recipientCode' is required");
        } else if(StringUtils.hasText(request.getOrigin()) && !Origin.IPA.equals(Origin.fromValue(request.getOrigin())) && request.getInstitutionLocationData() == null){
            throw new ValidationException(LOCATION_INFO_IS_REQUIRED);
        }
        onboardingService.autoApprovalOnboardingProduct(onboardingResourceMapper.toEntity(request));
        log.trace("onboarding end");
    }

    @PostMapping(value = "/pda/{injectionInstitutionType}")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "", notes = "${swagger.external_api.onboarding.api.injectionAutoApprovalOnboarding}")
    public void autoApprovalOnboardingFromPda(@ApiParam("${swagger.external_api.institutions.model.injectionInstitutionType}")
                                              @PathVariable("injectionInstitutionType")
                                              String injectionInstitutionType,
                                              @RequestBody @Valid PdaOnboardingDto request) {
        log.trace("onboarding start");
        log.debug("onboarding request = {}", request);
        onboardingService.autoApprovalOnboardingFromPda(onboardingResourceMapper.toEntity(request), injectionInstitutionType);
        log.trace("onboarding end");
    }

    /**
     * @deprecated (to be removed)
     */
    @Deprecated(forRemoval = true)
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
    @ApiOperation(value = "", notes = "${swagger.external_api.onboarding.api.pdaAutoApprovalOnboarding}")
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

}
