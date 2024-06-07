package it.pagopa.selfcare.external_api.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.commons.web.model.Problem;
import it.pagopa.selfcare.external_api.core.OnboardingService;
import it.pagopa.selfcare.external_api.model.user.RelationshipInfo;
import it.pagopa.selfcare.external_api.web.model.mapper.OnboardingMapper;
import it.pagopa.selfcare.external_api.web.model.mapper.OnboardingResourceMapper;
import it.pagopa.selfcare.external_api.web.model.mapper.RelationshipMapper;
import it.pagopa.selfcare.external_api.web.model.onboarding.OnboardingImportDto;
import it.pagopa.selfcare.external_api.web.model.onboarding.OnboardingInstitutionUsersRequest;
import it.pagopa.selfcare.external_api.web.model.onboarding.OnboardingProductDto;
import it.pagopa.selfcare.external_api.web.model.user.RelationshipResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.time.OffsetDateTime;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping(value = "/v2/onboarding", produces = APPLICATION_JSON_VALUE)
@Api(tags = "Onboarding")
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
        onboardingService.oldContractOnboardingV2(OnboardingMapper.toOnboardingData(externalInstitutionId, request));
        log.trace("oldContractonboarding end");
    }

    /**
     * The function persist user on registry if not exists and add relation with institution-product
     *
     * @param request OnboardingInstitutionUsersRequest
     * @return no content
     * * Code: 204, Message: successful operation
     * * Code: 404, Message: Not found, DataType: Problem
     * * Code: 400, Message: Invalid request, DataType: Problem
     */
    @ResponseStatus(HttpStatus.OK)
    @Tag(name = "support")
    @Tag(name = "Onboarding")
    @ApiOperation(value = "${swagger.mscore.onboarding.users}", notes = "${swagger.mscore.onboarding.users}")
    @PostMapping(value = "/users")
    public ResponseEntity<List<RelationshipResult>> onboardingInstitutionUsers(@RequestBody @Valid OnboardingInstitutionUsersRequest request,
                                                                               Authentication authentication) {
        SelfCareUser selfCareUser = (SelfCareUser) authentication.getPrincipal();
        List<RelationshipInfo> response = onboardingService.onboardingUsers(onboardingResourceMapper.toOnboardingUsersRequest(request), selfCareUser.getUserName(), selfCareUser.getSurname());
        return ResponseEntity.ok().body(RelationshipMapper.toRelationshipResultList(response));
    }

}
