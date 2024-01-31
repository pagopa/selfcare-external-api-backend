package it.pagopa.selfcare.external_api.web.controller;

import it.pagopa.selfcare.external_api.core.OnboardingService;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingData;
import it.pagopa.selfcare.external_api.web.config.WebTestConfig;
import it.pagopa.selfcare.external_api.web.model.mapper.OnboardingResourceMapperImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static it.pagopa.selfcare.external_api.web.controller.OnboardingControllerTest.FIELD_PSP_DATA_IS_REQUIRED_FOR_PSP_INSTITUTION_ONBOARDING;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = {OnboardingV2Controller.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {OnboardingV2Controller.class, WebTestConfig.class, OnboardingResourceMapperImpl.class})
public class OnboardingV2ControllerTest {

    private static final String BASE_URL = "/v2/onboarding";

    @Autowired
    protected MockMvc mvc;

    @MockBean
    private OnboardingService onboardingServiceMock;

    @Test
    void onboardingTest(@Value("classpath:stubs/onboardingSubunitDto.json") Resource onboardingDto) throws Exception {
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
                .autoApprovalOnboardingProductV2(any(OnboardingData.class));
        verifyNoMoreInteractions(onboardingServiceMock);
    }

    @Test
    void onboardingInvalidPspProductRequest(@Value("classpath:stubs/invalidOnboardingSubunitDto.json") Resource onboardingDto) throws Exception {
        // when
        mvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL)
                        .content(onboardingDto.getInputStream().readAllBytes())
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is(FIELD_PSP_DATA_IS_REQUIRED_FOR_PSP_INSTITUTION_ONBOARDING)));
        // then
        verifyNoInteractions(onboardingServiceMock);
    }

    @Test
    void onboardingInvalidSaOnboardingRequest(@Value("classpath:stubs/invalidSaOnboardingProductDto.json") Resource onboardingDto) throws Exception {
        // when
        mvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL)
                        .content(onboardingDto.getInputStream().readAllBytes())
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("Field 'recipientCode' is required")));

        // then
        verifyNoInteractions(onboardingServiceMock);
    }
}