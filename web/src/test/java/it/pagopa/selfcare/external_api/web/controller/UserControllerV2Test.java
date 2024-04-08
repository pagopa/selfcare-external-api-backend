package it.pagopa.selfcare.external_api.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.external_api.core.UserService;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionResponse;
import it.pagopa.selfcare.external_api.model.user.Certification;
import it.pagopa.selfcare.external_api.model.user.CertifiedField;
import it.pagopa.selfcare.external_api.model.user.User;
import it.pagopa.selfcare.external_api.model.user.UserInfoWrapper;
import it.pagopa.selfcare.external_api.web.config.WebTestConfig;
import it.pagopa.selfcare.external_api.web.model.mapper.UserInfoResourceMapperImpl;
import it.pagopa.selfcare.external_api.web.model.user.SearchUserDto;
import it.pagopa.selfcare.external_api.web.model.user.UserInfoResource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = {UserV2Controller.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {UserV2Controller.class, WebTestConfig.class, UserInfoResourceMapperImpl.class})
class UserControllerV2Test {

    private static final String BASE_URL = "/v2/users";
    private static final String fiscalCode = "MNCCSD01R13A757G";

    @Autowired
    protected MockMvc mvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @MockBean
    private UserService userService;

    @Test
    void getUserInfo() throws Exception {
        //given
        SearchUserDto searchUserDto = new SearchUserDto();
        searchUserDto.setFiscalCode(fiscalCode);
        UserInfoWrapper userWrapper = new UserInfoWrapper();
        userWrapper.setUser(this.buildUser());
        userWrapper.setOnboardedInstitutions(List.of(new OnboardedInstitutionResponse()));
        when(userService.getUserInfoV2(anyString(), any()))
                .thenReturn(userWrapper);
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL)
                        .content(objectMapper.writeValueAsString(searchUserDto))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
        //then
        UserInfoResource response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertNotNull(response);
        assertNotNull(userWrapper.getUser());
        assertNotNull(userWrapper.getOnboardedInstitutions());
        assertEquals(response.getUser().getName(), userWrapper.getUser().getName().getValue());
        assertEquals(response.getUser().getEmail(), userWrapper.getUser().getEmail().getValue());
    }

    private User buildUser() {
        User user = new User();
        user.setFiscalCode(fiscalCode);
        CertifiedField<String> fieldName = new CertifiedField<>();
        fieldName.setCertification(Certification.SPID);
        fieldName.setValue("testName");
        CertifiedField<String> fieldEmail = new CertifiedField<>();
        fieldName.setCertification(Certification.SPID);
        fieldName.setValue("email");
        user.setName(fieldName);
        user.setEmail(fieldEmail);
        return user;
    }

}
