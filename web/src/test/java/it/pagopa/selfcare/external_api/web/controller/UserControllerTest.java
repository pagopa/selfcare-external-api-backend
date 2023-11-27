package it.pagopa.selfcare.external_api.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.external_api.core.UserService;
import it.pagopa.selfcare.external_api.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionResponse;
import it.pagopa.selfcare.external_api.model.user.*;
import it.pagopa.selfcare.external_api.web.config.WebTestConfig;
import it.pagopa.selfcare.external_api.web.model.mapper.UserInfoResourceMapperImpl;
import it.pagopa.selfcare.external_api.web.model.user.SearchUserDto;
import it.pagopa.selfcare.external_api.web.model.user.UserDetailsResource;
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

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = {UserController.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {UserController.class, WebTestConfig.class, UserInfoResourceMapperImpl.class})
class UserControllerTest {

    private static final String BASE_URL = "/users";
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
        when(userService.getUserInfo(anyString(), any()))
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

    @Test
    void getUserProductInfo() throws Exception {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        String userId = "userId";
        UserDetailsWrapper userDetailsWrapper = new UserDetailsWrapper();
        userDetailsWrapper.setUserId(userId);
        userDetailsWrapper.setInstitutionId(institutionId);
        userDetailsWrapper.setProductDetails(buildProductDetails());
        when(userService.getUserOnboardedProductDetails(anyString(), anyString(), anyString())).thenReturn(userDetailsWrapper);
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{id}/onboarded-product", userId)
                        .queryParam("institutionId", institutionId)
                        .queryParam("productId", productId)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
        //then
        UserDetailsResource response =  objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertNotNull(response);
        assertNotNull(response.getOnboardedProductDetails());
    }
    @Test
    void getUserInfoThrowsResourceNotFound() {
        when(userService.getUserInfo("12", List.of()))
                .thenThrow(new ResourceNotFoundException("User with fiscal code" + 12 + " not found"));
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserInfo("12", List.of()));
        verify(userService).getUserInfo(any(), any());
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

    private ProductDetails buildProductDetails(){
        ProductDetails product = new ProductDetails();
        product.setProductId("productId");
        product.setRoles(List.of("role"));
        product.setRole(PartyRole.MANAGER);
        product.setCreatedAt(OffsetDateTime.now());
        return product;
    }
}
