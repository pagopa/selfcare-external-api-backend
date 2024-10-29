package it.pagopa.selfcare.external_api.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.commons.web.model.Problem;
import it.pagopa.selfcare.external_api.mapper.OnboardingMapperCustom;
import it.pagopa.selfcare.external_api.mapper.OnboardingResourceMapper;
import it.pagopa.selfcare.external_api.mapper.RelationshipMapper;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingImportDto;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingImportProductDto;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingInstitutionUsersRequest;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingProductDto;
import it.pagopa.selfcare.external_api.model.user.RelationshipInfo;
import it.pagopa.selfcare.external_api.model.user.RelationshipResult;
import it.pagopa.selfcare.external_api.service.OnboardingService;
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

  @Autowired
  public OnboardingV2Controller(
      OnboardingService onboardingService, OnboardingResourceMapper onboardingResourceMapper) {
    this.onboardingService = onboardingService;
    this.onboardingResourceMapper = onboardingResourceMapper;
  }

  @Tag(name = "internal-v1")
  @ApiResponse(
      responseCode = "403",
      description = "Forbidden",
      content = {
        @Content(
            mediaType = APPLICATION_PROBLEM_JSON_VALUE,
            schema = @Schema(implementation = Problem.class))
      })
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @ApiOperation(value = "", notes = "${swagger.onboarding.institutions.api.onboarding.subunit}")
  public void onboarding(@RequestBody @Valid OnboardingProductDto request) {
    log.trace("onboarding start");
    log.debug("onboarding request = {}", sanitize(request.toString()));
    onboardingService.autoApprovalOnboardingProductV2(onboardingResourceMapper.toEntity(request));
    log.trace("onboarding end");
  }

  @Tag(name = "internal-v1")
  @ApiResponse(
      responseCode = "403",
      description = "Forbidden",
      content = {
        @Content(
            mediaType = APPLICATION_PROBLEM_JSON_VALUE,
            schema = @Schema(implementation = Problem.class))
      })
  @PostMapping(value = "/import")
  @ResponseStatus(HttpStatus.CREATED)
  @ApiOperation(value = "", notes = "${swagger.onboarding.institutions.api.onboarding.import}")
  public void onboardingImport(@RequestBody @Valid OnboardingImportProductDto request) {
    log.trace("onboardingImport start");
    String sanitizedRequest = sanitize(request.toString());
    log.debug("onboardingImport request = {}", sanitizedRequest);
    onboardingService.autoApprovalOnboardingImportProductV2(
        onboardingResourceMapper.toEntity(request));
    log.trace("onboardingImport end");
  }

  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "409",
            description = "Conflict",
            content = {
              @Content(
                  mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                  schema = @Schema(implementation = Problem.class))
            }),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content = {
              @Content(
                  mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                  schema = @Schema(implementation = Problem.class))
            })
      })
  @PostMapping(value = "/{externalInstitutionId}")
  @ResponseStatus(HttpStatus.CREATED)
  @ApiOperation(value = "", notes = "${swagger.external_api.onboarding.api.onboardingOldContract}")
  public void oldContractOnboarding(
      @ApiParam("${swagger.external_api.institutions.model.externalId}")
          @PathVariable("externalInstitutionId")
          String externalInstitutionId,
      @RequestBody @Valid OnboardingImportDto request) {
    log.trace("oldContractonboarding start");
    String sanitizedRequest = sanitize(request.toString());
    log.debug(
        "oldContractonboarding institutionId = {}, request = {}",
        externalInstitutionId,
        sanitizedRequest);
    if (request.getImportContract().getOnboardingDate().compareTo(OffsetDateTime.now()) > 0) {
      throw new ValidationException(
          "Invalid onboarding date: the onboarding date must be prior to the current date.");
    }
    onboardingService.oldContractOnboardingV2(
        OnboardingMapperCustom.toOnboardingData(externalInstitutionId, request));
    log.trace("oldContractonboarding end");
  }

  /**
   * The function persist user on registry if not exists and add relation with institution-product
   *
   * @param request OnboardingInstitutionUsersRequest
   * @return no content * Code: 204, Message: successful operation * Code: 404, Message: Not found,
   *     DataType: Problem * Code: 400, Message: Invalid request, DataType: Problem
   */
  @ResponseStatus(HttpStatus.OK)
  @Tag(name = "internal-v1")
  @Tag(name = "support")
  @Tag(name = "Onboarding")
  @ApiOperation(
      value = "${swagger.mscore.onboarding.users}",
      notes = "${swagger.mscore.onboarding.users}")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "OK",
            content =
                @Content(
                    array =
                        @ArraySchema(schema = @Schema(implementation = RelationshipResult.class)),
                    mediaType = "application/json")),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request",
            content =
                @Content(
                    schema = @Schema(implementation = Problem.class),
                    mediaType = "application/problem+json")),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content =
                @Content(
                    schema = @Schema(implementation = Problem.class),
                    mediaType = "application/problem+json")),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content =
                @Content(
                    schema = @Schema(implementation = Problem.class),
                    mediaType = "application/problem+json")),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found",
            content =
                @Content(
                    schema = @Schema(implementation = Problem.class),
                    mediaType = "application/problem+json"))
      })
  @PostMapping(value = "/users")
  public ResponseEntity<List<RelationshipResult>> onboardingInstitutionUsers(
      @RequestBody @Valid OnboardingInstitutionUsersRequest request,
      Authentication authentication) {
    SelfCareUser selfCareUser = (SelfCareUser) authentication.getPrincipal();
    List<RelationshipInfo> response =
        onboardingService.onboardingUsers(
            onboardingResourceMapper.toOnboardingUsersRequest(request),
            selfCareUser.getUserName(),
            selfCareUser.getSurname());
    return ResponseEntity.ok().body(RelationshipMapper.toRelationshipResultList(response));
  }

  private String sanitize(String input) {
    return input.replaceAll("[\\r\\n]", "_");
  }
}
