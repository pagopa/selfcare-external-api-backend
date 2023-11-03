package it.pagopa.selfcare.external_api.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.external_api.core.OnboardingService;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingData;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingImportData;
import it.pagopa.selfcare.external_api.model.onboarding.PdaOnboardingData;
import it.pagopa.selfcare.external_api.web.config.WebTestConfig;
import it.pagopa.selfcare.external_api.web.model.mapper.OnboardingResourceMapperImpl;
import it.pagopa.selfcare.external_api.web.model.onboarding.OnboardingImportDto;
import it.pagopa.selfcare.external_api.web.model.user.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = {OnboardingController.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {OnboardingController.class, WebTestConfig.class, OnboardingResourceMapperImpl.class})
class OnboardingControllerTest {

    private static final String BASE_URL = "/onboarding";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    private OnboardingService onboardingServiceMock;

    @Test
    void oldContractOnboarding(@Value("classpath:stubs/onboardingImportDto.json") Resource onboardingImportDto) throws Exception {
        // given
        String institutionId = "institutionId";
        // when
        mvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL + "/{institutionId}", institutionId)
                        .content(onboardingImportDto.getInputStream().readAllBytes())
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andExpect(content().string(emptyString()));
        // then
        verify(onboardingServiceMock, times(1))
                .oldContractOnboarding(any(OnboardingImportData.class));
        verifyNoMoreInteractions(onboardingServiceMock);
    }

    @Test
    void oldContractOnboarding_invalidDate() throws Exception {
        // given
        String institutionId = "institutionId";
        OnboardingImportDto onboardingImportDto = TestUtils.mockInstance(new OnboardingImportDto());
        onboardingImportDto.getImportContract().setOnboardingDate(OffsetDateTime.now().plusHours(1));
        UserDto userDto = TestUtils.mockInstance(new UserDto(), "setEmail");
        userDto.setEmail("email@example.com");
        onboardingImportDto.setUsers(List.of(userDto));
        // when
        mvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL + "/{institutionId}", institutionId)
                        .content(objectMapper.writeValueAsString(onboardingImportDto))
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("Invalid onboarding date: the onboarding date must be prior to the current date.")));
        // then
        verifyNoInteractions(onboardingServiceMock);
    }

    @Test
    void onboarding(@Value("classpath:stubs/onboardingDto.json") Resource onboardingDto) throws Exception {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        // when
        mvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL + "/{institutionId}/products/{productId}", institutionId, productId)
                        .content(onboardingDto.getInputStream().readAllBytes())
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andExpect(content().string(emptyString()));
        // then
        verify(onboardingServiceMock, times(1))
                .autoApprovalOnboarding(any(OnboardingData.class));
        verifyNoMoreInteractions(onboardingServiceMock);
    }

    @Test
    void onboardingSubunit(@Value("classpath:stubs/onboardingSubunitDto.json") Resource onboardingSubunitDto) throws Exception {
        // when
        mvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL)
                        .content(onboardingSubunitDto.getInputStream().readAllBytes())
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andExpect(content().string(emptyString()));
        // then
        verify(onboardingServiceMock, times(1))
                .autoApprovalOnboardingProduct(any(OnboardingData.class));
        verifyNoMoreInteractions(onboardingServiceMock);
    }

    @Test
    void onboardingECFromPDA(@Value("classpath:stubs/onboardingFromPDA.json") Resource onboardingSubunitDto) throws Exception {
        String injectionInstitutionType = "PT";
        // when
        mvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL + "/pda/{injectionInstitutionType}",injectionInstitutionType)
                        .content(onboardingSubunitDto.getInputStream().readAllBytes())
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andExpect(content().string(emptyString()));
        // then
        verify(onboardingServiceMock, times(1))
                .autoApprovalOnboardingFromPda(any(PdaOnboardingData.class), any());
        verifyNoMoreInteractions(onboardingServiceMock);
    }

    @Test
    void onboardingPTFromPDA(@Value("classpath:stubs/onboardingFromPDA.json") Resource onboardingSubunitDto) throws Exception {
        String injectionInstitutionType = "PT";
        // when
        mvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL + "/pda/{injectionInstitutionType}",injectionInstitutionType)
                        .content(onboardingSubunitDto.getInputStream().readAllBytes())
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andExpect(content().string(emptyString()));
        // then
        verify(onboardingServiceMock, times(1))
                .autoApprovalOnboardingFromPda(any(PdaOnboardingData.class), any());
        verifyNoMoreInteractions(onboardingServiceMock);
    }

    @Test
    void onboardingValidPspProductRequest(@Value("classpath:stubs/validOnboardingSubunitDto.json") Resource onboardingDto) throws Exception {
        // when
        mvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL)
                        .content(onboardingDto.getInputStream().readAllBytes())
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andExpect(content().string(emptyString()));
        // then
        verify(onboardingServiceMock, times(1))
                .autoApprovalOnboardingProduct(any(OnboardingData.class));
        verifyNoMoreInteractions(onboardingServiceMock);
    }

    @Test
    void onboardingInvalidPspProductRequest(@Value("classpath:stubs/invalidOnboardingSubunitDto.json") Resource onboardingDto) throws Exception {
        // when
        performOnboardingCall(onboardingDto)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("Field 'pspData' is required for PSP institution onboarding")));
        // then
        verifyNoInteractions(onboardingServiceMock);
    }

    @Test
    void onboardingPspValidRequest(@Value("classpath:stubs/validPspOnboardingDto.json") Resource onboardingDto) throws Exception {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        // when
        mvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL + "/{institutionId}/products/{productId}", institutionId, productId)
                        .content(onboardingDto.getInputStream().readAllBytes())
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andExpect(content().string(emptyString()));
        // then
        verify(onboardingServiceMock, times(1))
                .autoApprovalOnboarding(any(OnboardingData.class));
        verifyNoMoreInteractions(onboardingServiceMock);
    }

    @Test
    void onboardingInvalidPspOnboardingRequest(@Value("classpath:stubs/invalidPspOnboardingDto.json") Resource onboardingDto) throws Exception {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        // when
        mvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL + "/{institutionId}/products/{productId}", institutionId, productId)
                        .content(onboardingDto.getInputStream().readAllBytes())
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("Field 'pspData' is required for PSP institution onboarding")));
        // then
        verifyNoInteractions(onboardingServiceMock);
    }

    @Test
    void verifyOnboarding() throws Exception {
        final String externalInstitutionId = "externalInstitutionId";
        final String productId = "productId";
        //when
        mvc.perform(MockMvcRequestBuilders
                        .head(BASE_URL + "/{externalInstitutionId}/products/{productId}", externalInstitutionId, productId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent());
    }

    @Test
    void onboardingInvalidSaOnboardingRequest(@Value("classpath:stubs/invalidSaOnboardingProductDto.json") Resource onboardingDto) throws Exception {
        // when
        performOnboardingCall(onboardingDto)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("Field 'recipientCode' is required")));

        // then
        verifyNoInteractions(onboardingServiceMock);
    }

    @Test
    void onboardingInvalidOnboardingRequestNotIPA(@Value("classpath:stubs/invalidOnboardingNotIPA.json") Resource onboardingDto) throws Exception {
        // when
        performOnboardingCall(onboardingDto)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("Validation failed")));
        // then
        verifyNoInteractions(onboardingServiceMock);
    }

    private ResultActions performOnboardingCall(Resource onboardingDto) throws Exception {
        return mvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL)
                        .content(onboardingDto.getInputStream().readAllBytes())
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE));
    }
}
