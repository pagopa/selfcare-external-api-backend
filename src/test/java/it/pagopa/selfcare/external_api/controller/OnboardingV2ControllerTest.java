package it.pagopa.selfcare.external_api.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.external_api.mapper.OnboardingResourceMapperImpl;
import it.pagopa.selfcare.external_api.model.onboarding.*;
import it.pagopa.selfcare.external_api.model.user.RelationshipInfo;
import it.pagopa.selfcare.external_api.service.OnboardingService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.validation.ValidationException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OnboardingV2ControllerTest extends BaseControllerTest {

  private static final String BASE_URL = "/v2/onboarding";

  @InjectMocks protected OnboardingV2Controller onboardingV2Controller;

  @Mock private OnboardingService onboardingServiceMock;

  @Spy private OnboardingResourceMapperImpl onboardingResourceMapperImpl;

  @BeforeEach
  void setUp() {
    super.setUp(onboardingV2Controller);
  }

  @Test
  void onboardingOk() throws Exception {

    ClassPathResource outputResource = new ClassPathResource("stubs/onboardingSubunitDto.json");
    String request =
        StringUtils.deleteWhitespace(
            new String(Files.readAllBytes(outputResource.getFile().toPath())));

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(BASE_URL)
                .content(request)
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isCreated())
        .andReturn();
  }

  @Test
  void onboardingWithoutUser() throws Exception {

    ClassPathResource outputResource = new ClassPathResource("stubs/onboardingSubunitDto.json");
    byte[] onboardingDtoFile = Files.readAllBytes(outputResource.getFile().toPath());
    OnboardingProductDto onboardingDto =
        objectMapper.readValue(onboardingDtoFile, new TypeReference<>() {});

    onboardingDto.setUsers(null);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(BASE_URL)
                .content(objectMapper.writeValueAsString(onboardingDto))
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest())
        .andReturn();
  }

  @Test
  void onboardingWithoutBilling() throws Exception {

    ClassPathResource outputResource = new ClassPathResource("stubs/onboardingSubunitDto.json");
    byte[] onboardingDtoFile = Files.readAllBytes(outputResource.getFile().toPath());
    OnboardingProductDto onboardingDto =
        objectMapper.readValue(onboardingDtoFile, new TypeReference<>() {});

    onboardingDto.setBillingData(new BillingDataDto());

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(BASE_URL)
                .content(objectMapper.writeValueAsString(onboardingDto))
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest())
        .andReturn();
  }

  @Test
  void onboardingWithoutInstitutionType() throws Exception {

    ClassPathResource outputResource = new ClassPathResource("stubs/onboardingSubunitDto.json");
    byte[] onboardingDtoFile = Files.readAllBytes(outputResource.getFile().toPath());
    OnboardingProductDto onboardingDto =
        objectMapper.readValue(onboardingDtoFile, new TypeReference<>() {});

    onboardingDto.setInstitutionType(null);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(BASE_URL)
                .content(objectMapper.writeValueAsString(onboardingDto))
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest())
        .andReturn();
  }

  @Test
  void onboardingWithoutProductId() throws Exception {

    ClassPathResource outputResource = new ClassPathResource("stubs/onboardingSubunitDto.json");
    byte[] onboardingDtoFile = Files.readAllBytes(outputResource.getFile().toPath());
    OnboardingProductDto onboardingDto =
        objectMapper.readValue(onboardingDtoFile, new TypeReference<>() {});

    onboardingDto.setProductId(null);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(BASE_URL)
                .content(objectMapper.writeValueAsString(onboardingDto))
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest())
        .andReturn();
  }

  @Test
  void onboardingWithoutGeographicTaxonomy() throws Exception {

    ClassPathResource outputResource = new ClassPathResource("stubs/onboardingSubunitDto.json");
    byte[] onboardingDtoFile = Files.readAllBytes(outputResource.getFile().toPath());
    OnboardingProductDto onboardingDto =
        objectMapper.readValue(onboardingDtoFile, new TypeReference<>() {});

    onboardingDto.setGeographicTaxonomies(null);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(BASE_URL)
                .content(objectMapper.writeValueAsString(onboardingDto))
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest())
        .andReturn();
  }

  @Test
  void oldContractOnboarding() throws Exception {

    String institutionId = "institutionId";

    byte[] onboardingImportDtoFile =
        Files.readAllBytes(Paths.get("src/test/resources", "stubs/onboardingImportDto.json"));
    OnboardingImportDto onboardingImportDto =
        objectMapper.readValue(onboardingImportDtoFile, new TypeReference<>() {});

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(BASE_URL + "/{institutionId}", institutionId)
                .content(objectMapper.writeValueAsString(onboardingImportDto))
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isCreated())
        .andExpect(content().string(emptyString()))
        .andReturn();
  }

  @Test
  void oldContractOnboardingWithInvalidOnboardingDate() throws Exception {

    String externalInstitutionId = "externalInstitutionId";

    byte[] onboardingImportDtoFile =
        Files.readAllBytes(Paths.get("src/test/resources", "stubs/onboardingImportDto.json"));
    OnboardingImportDto onboardingImportDto =
        objectMapper.readValue(onboardingImportDtoFile, new TypeReference<>() {});
    onboardingImportDto.getImportContract().setOnboardingDate(OffsetDateTime.now().plusDays(2));

    Assertions.assertThrows(
        ValidationException.class,
        () ->
            onboardingV2Controller.oldContractOnboarding(
                externalInstitutionId, onboardingImportDto),
        "Invalid onboarding date: the onboarding date must be prior to the current date.");
  }

  @Test
  void onboardingInstitutionUsers() throws Exception {

    byte[] onboardingImportDtoFile =
        Files.readAllBytes(
            Paths.get("src/test/resources", "stubs/OnboardingInstitutionUserRequest.json"));
    OnboardingInstitutionUsersRequest onboardingInstitutionUsersRequest =
        objectMapper.readValue(onboardingImportDtoFile, new TypeReference<>() {});

    ClassPathResource relationshipInfosFile =
        new ClassPathResource("expectations/RelationshipInfo.json");
    List<RelationshipInfo> relationshipInfos =
        objectMapper.readValue(
            Files.readAllBytes(relationshipInfosFile.getFile().toPath()), new TypeReference<>() {});

    ClassPathResource outputResource =
        new ClassPathResource("expectations/RelationshipResult.json");
    String expectedResource =
        StringUtils.deleteWhitespace(
            new String(Files.readAllBytes(outputResource.getFile().toPath())));

    when(onboardingServiceMock.onboardingUsers(
            any(OnboardingUsersRequest.class), anyString(), anyString()))
        .thenReturn(relationshipInfos);

    SelfCareUser selfCareUser = SelfCareUser.builder("id").name("nome").surname("cognome").build();
    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(selfCareUser);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(BASE_URL + "/users")
                .principal(authentication)
                .content(new ObjectMapper().writeValueAsString(onboardingInstitutionUsersRequest))
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(content().string(expectedResource))
        .andExpect(jsonPath("$", hasSize(3)))
        .andExpect(status().isOk());

    verify(onboardingServiceMock, times(1))
        .onboardingUsers(any(OnboardingUsersRequest.class), anyString(), anyString());
    verifyNoMoreInteractions(onboardingServiceMock);
  }

  @Test
  void onboardingInstitutionUsersWithoutUserToOnboard() throws Exception {

    byte[] onboardingImportDtoFile =
        Files.readAllBytes(
            Paths.get("src/test/resources", "stubs/OnboardingInstitutionUserRequest.json"));
    OnboardingInstitutionUsersRequest onboardingInstitutionUsersRequest =
        objectMapper.readValue(onboardingImportDtoFile, new TypeReference<>() {});
    onboardingInstitutionUsersRequest.setUsers(null);

    Authentication authentication = mock(Authentication.class);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(BASE_URL + "/users")
                .principal(authentication)
                .content(new ObjectMapper().writeValueAsString(onboardingInstitutionUsersRequest))
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest());
  }

  @Test
  void onboardingInstitutionUsersWithoutInstitutionTaxCode() throws Exception {

    byte[] onboardingImportDtoFile =
        Files.readAllBytes(
            Paths.get("src/test/resources", "stubs/OnboardingInstitutionUserRequest.json"));
    OnboardingInstitutionUsersRequest onboardingInstitutionUsersRequest =
        objectMapper.readValue(onboardingImportDtoFile, new TypeReference<>() {});
    onboardingInstitutionUsersRequest.setInstitutionTaxCode(null);

    Authentication authentication = mock(Authentication.class);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(BASE_URL + "/users")
                .principal(authentication)
                .content(new ObjectMapper().writeValueAsString(onboardingInstitutionUsersRequest))
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest());
  }

  @Test
  void onboardingInstitutionUsersWithoutProductId() throws Exception {

    byte[] onboardingImportDtoFile =
        Files.readAllBytes(
            Paths.get("src/test/resources", "stubs/OnboardingInstitutionUserRequest.json"));
    OnboardingInstitutionUsersRequest onboardingInstitutionUsersRequest =
        objectMapper.readValue(onboardingImportDtoFile, new TypeReference<>() {});
    onboardingInstitutionUsersRequest.setProductId(null);

    Authentication authentication = mock(Authentication.class);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(BASE_URL + "/users")
                .principal(authentication)
                .content(new ObjectMapper().writeValueAsString(onboardingInstitutionUsersRequest))
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest());
  }

  @Test
  void onboardingImport() throws Exception {

    ClassPathResource outputResource = new ClassPathResource("stubs/onboardingSubunitDto.json");
    String request =
        StringUtils.deleteWhitespace(
            new String(Files.readAllBytes(outputResource.getFile().toPath())));

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(BASE_URL + "/import")
                .content(request)
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isCreated())
        .andReturn();
  }
}
