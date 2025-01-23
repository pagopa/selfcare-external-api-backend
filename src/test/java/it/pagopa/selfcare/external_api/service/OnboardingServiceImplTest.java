package it.pagopa.selfcare.external_api.service;

import static it.pagopa.selfcare.onboarding.common.InstitutionType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.InstitutionResponse;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.InstitutionsResponse;
import it.pagopa.selfcare.external_api.client.MsCoreInstitutionApiClient;
import it.pagopa.selfcare.external_api.client.MsOnboardingControllerApi;
import it.pagopa.selfcare.external_api.client.MsPartyRegistryProxyRestClient;
import it.pagopa.selfcare.external_api.client.MsUserApiRestClient;
import it.pagopa.selfcare.external_api.exception.ResourceNotFoundException;
import it.pagopa.selfcare.external_api.mapper.OnboardingMapperImpl;
import it.pagopa.selfcare.external_api.mapper.UserResourceMapper;
import it.pagopa.selfcare.external_api.model.institution.GeographicTaxonomy;
import it.pagopa.selfcare.external_api.model.onboarding.InstitutionUpdate;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingAggregatorImportDto;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingData;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingUsersRequest;
import it.pagopa.selfcare.external_api.model.user.RelationshipInfo;
import it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.OnboardingAggregationImportRequest;
import it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.OnboardingResponse;
import it.pagopa.selfcare.registry_proxy.generated.openapi.v1.dto.InstitutionResource;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;

@ExtendWith({MockitoExtension.class})
class OnboardingServiceImplTest extends BaseServiceTestUtils {
  @InjectMocks private OnboardingServiceImpl onboardingService;

  @Mock private MsOnboardingControllerApi onboardingControllerApi;

  @Mock private MsCoreInstitutionApiClient institutionApiClient;

  @Mock private MsUserApiRestClient msUserApiRestClient;

  @Spy private UserResourceMapper userResourceMapper;

  @Spy private OnboardingMapperImpl onboardingMapper;

  @Mock private MsPartyRegistryProxyRestClient msPartyRegistryProxyRestClient;

  @Override
  @BeforeEach
  public void setUp() {
    super.setUp();
  }

  @Test
  void oldContractOnboardingTest() {
    OnboardingData onboardingData = new OnboardingData();
    onboardingData.setInstitutionExternalId("externalId");
    onboardingData.setInstitutionUpdate(new InstitutionUpdate());
    Assertions.assertDoesNotThrow(() -> onboardingService.oldContractOnboardingV2(onboardingData));
  }

  @Test
  void oldContractOnboardingTestWithOrigin() {
    OnboardingData onboardingData = new OnboardingData();
    onboardingData.setInstitutionExternalId("externalId");
    onboardingData.setInstitutionUpdate(new InstitutionUpdate());
    onboardingData.setOrigin("ADE");
    Assertions.assertDoesNotThrow(() -> onboardingService.oldContractOnboardingV2(onboardingData));
  }

  @Test
  void autoApprovalOnboardingProductV2TestPA() {
    OnboardingData onboardingData = new OnboardingData();
    onboardingData.setInstitutionExternalId("externalId");
    InstitutionUpdate institutionUpdate = new InstitutionUpdate();
    onboardingData.setInstitutionType(PA);
    institutionUpdate.setGeographicTaxonomies(List.of(new GeographicTaxonomy()));
    onboardingData.setInstitutionUpdate(institutionUpdate);
    Assertions.assertDoesNotThrow(
        () -> onboardingService.autoApprovalOnboardingProductV2(onboardingData));
  }

  @Test
  void autoApprovalOnboardingProductV2TestPSP() {
    OnboardingData onboardingData = new OnboardingData();
    onboardingData.setInstitutionExternalId("externalId");
    onboardingData.setInstitutionUpdate(new InstitutionUpdate());
    onboardingData.setInstitutionType(PSP);
    Assertions.assertDoesNotThrow(
        () -> onboardingService.autoApprovalOnboardingProductV2(onboardingData));
  }

  @Test
  void autoApprovalOnboardingProductV2Test() {
    OnboardingData onboardingData = new OnboardingData();
    onboardingData.setInstitutionUpdate(new InstitutionUpdate());
    onboardingData.setInstitutionExternalId("externalId");
    onboardingData.setInstitutionType(PG);
    Assertions.assertDoesNotThrow(
        () -> onboardingService.autoApprovalOnboardingProductV2(onboardingData));
  }

  @Test
  void autoApprovalOnboardingImportProductV2Test() {
    OnboardingData onboardingData = new OnboardingData();
    onboardingData.setInstitutionExternalId("externalId");
    InstitutionUpdate institutionUpdate = new InstitutionUpdate();
    institutionUpdate.setGeographicTaxonomies(List.of(new GeographicTaxonomy()));
    onboardingData.setInstitutionUpdate(institutionUpdate);
    onboardingData.setInstitutionType(PSP);
    Assertions.assertDoesNotThrow(
        () -> onboardingService.autoApprovalOnboardingImportProductV2(onboardingData));
  }

  @Test
  void onboardingUsers_noInstitutionFound() throws Exception {
    ClassPathResource inputResource =
        new ClassPathResource("expectations/OnboardingUsersRequest.json");
    byte[] onboardingUsersRequestStream = Files.readAllBytes(inputResource.getFile().toPath());
    OnboardingUsersRequest onboardingUsersRequest =
        objectMapper.readValue(onboardingUsersRequestStream, OnboardingUsersRequest.class);
    when(institutionApiClient._getInstitutionsUsingGET(
            onboardingUsersRequest.getInstitutionTaxCode(),
            onboardingUsersRequest.getInstitutionSubunitCode(),
            null,
            null,
            null))
        .thenReturn(
            ResponseEntity.ok(new InstitutionsResponse().institutions(Collections.emptyList())));
    Assertions.assertThrows(
        ResourceNotFoundException.class,
        () -> onboardingService.onboardingUsers(onboardingUsersRequest, "userName", "surname"),
        "Institution not found for given value");
  }

  @Test
  void onboardingUsers_happyPath() throws Exception {
    ClassPathResource onboardingUsersRequestInputResource =
        new ClassPathResource("expectations/OnboardingUsersRequest.json");
    byte[] onboardingUsersRequestStream =
        Files.readAllBytes(onboardingUsersRequestInputResource.getFile().toPath());
    OnboardingUsersRequest onboardingUsersRequest =
        objectMapper.readValue(onboardingUsersRequestStream, OnboardingUsersRequest.class);

    ClassPathResource institutionInputResource =
        new ClassPathResource("expectations/InstitutionResponse.json");
    byte[] institutionStream = Files.readAllBytes(institutionInputResource.getFile().toPath());
    List<InstitutionResponse> institutions =
        objectMapper.readValue(institutionStream, new TypeReference<>() {});
    when(institutionApiClient._getInstitutionsUsingGET(
            onboardingUsersRequest.getInstitutionTaxCode(),
            onboardingUsersRequest.getInstitutionSubunitCode(),
            null,
            null,
            null))
        .thenReturn(ResponseEntity.ok(new InstitutionsResponse().institutions(institutions)));
    when(msUserApiRestClient._createOrUpdateByFiscalCode(any()))
        .thenReturn(ResponseEntity.ok("userId"));
    when(msUserApiRestClient._createOrUpdateByUserId(any(), any()))
        .thenReturn(ResponseEntity.ok().build());
    List<RelationshipInfo> result =
        onboardingService.onboardingUsers(onboardingUsersRequest, "userName", "surname");

    assertEquals(2, result.size());
  }

  @Test
  void onboardingAggregateImportTest() throws IOException {
    // given
    ClassPathResource onboardingInputRequest =
        new ClassPathResource("expectations/onboardingAggregateImportDto.json");
    byte[] onboardingInputRequestStream =
        Files.readAllBytes(onboardingInputRequest.getFile().toPath());
    OnboardingAggregatorImportDto onboardingAggregatorImportDto =
        objectMapper.readValue(onboardingInputRequestStream, OnboardingAggregatorImportDto.class);

    when(msPartyRegistryProxyRestClient._findInstitutionUsingGET("01234567890", null, null))
        .thenReturn(ResponseEntity.ok(new InstitutionResource()));

    when(msPartyRegistryProxyRestClient._findInstitutionUsingGET("taxCodeRequest", null, null))
        .thenReturn(ResponseEntity.ok(new InstitutionResource()));

    // then
    Assertions.assertDoesNotThrow(
        () ->
            onboardingService.onboardingAggregatorImportBuildRequest(
                onboardingAggregatorImportDto, "taxCodeRequest"));
  }

  @Test
  void aggregationImportTest() {
    // given
    when(onboardingControllerApi._onboardingAggregationImport(
            new OnboardingAggregationImportRequest()))
        .thenReturn(ResponseEntity.ok(new OnboardingResponse()));

    // then
    Assertions.assertDoesNotThrow(
        () -> onboardingService.aggregationImport(new OnboardingAggregationImportRequest()));
  }
}
