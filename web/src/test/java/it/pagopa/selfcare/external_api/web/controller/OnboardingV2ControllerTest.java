package it.pagopa.selfcare.external_api.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.external_api.core.OnboardingService;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingData;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingUsersRequest;
import it.pagopa.selfcare.external_api.model.user.RelationshipInfo;
import it.pagopa.selfcare.external_api.web.config.WebTestConfig;
import it.pagopa.selfcare.external_api.web.model.mapper.OnboardingResourceMapperImpl;
import it.pagopa.selfcare.external_api.web.model.onboarding.OnboardingInstitutionUsersRequest;
import it.pagopa.selfcare.external_api.web.model.user.Person;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.hamcrest.Matchers.emptyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                .oldContractOnboardingV2(any(OnboardingData.class));
        verifyNoMoreInteractions(onboardingServiceMock);
    }

    @Test
    void onboardingInstitutionUsers() throws Exception {
        OnboardingInstitutionUsersRequest request = new OnboardingInstitutionUsersRequest();
        request.setInstitutionTaxCode("taxCode");
        request.setProductId("productCode");
        request.setUsers(List.of(new Person()));

        Institution institution = new Institution();
        institution.setId("institutionId");
        RelationshipInfo relationshipInfo = new RelationshipInfo();
        relationshipInfo.setInstitution(institution);
        when(onboardingServiceMock.onboardingUsers(any(OnboardingUsersRequest.class), anyString(), anyString())).thenReturn(List.of(relationshipInfo));
        SelfCareUser selfCareUser = SelfCareUser.builder("id").name("nome").surname("cognome").build();
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(selfCareUser);

        mvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL + "/users")
                        .principal(authentication)
                        .content(new ObjectMapper().writeValueAsString(request))
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        verify(onboardingServiceMock, times(1)).onboardingUsers(any(OnboardingUsersRequest.class), anyString(), anyString());
        verifyNoMoreInteractions(onboardingServiceMock);
    }
}