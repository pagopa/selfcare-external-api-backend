package it.pagopa.selfcare.external_api.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.external_api.core.UserService;
import it.pagopa.selfcare.external_api.model.user.ProductDetails;
import it.pagopa.selfcare.external_api.model.user.RelationshipState;
import it.pagopa.selfcare.external_api.model.user.UserDetailsWrapper;
import it.pagopa.selfcare.external_api.model.user.UserInfoWrapper;
import it.pagopa.selfcare.external_api.web.model.user.SearchUserDto;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerV2Test {

    private static final String BASE_URL = "/v2/users";

    @InjectMocks
    protected UserV2Controller userV2Controller;

    @Mock
    private UserService userService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(userV2Controller)
                .build();
    }


    @Test
    void getUserInfoFound() throws Exception {

        ClassPathResource inputResource = new ClassPathResource("expectations/UserInfoWrapper.json");
        UserInfoWrapper userInfoWrapper = objectMapper.readValue(Files.readAllBytes(inputResource.getFile().toPath()), new TypeReference<>() {});

        ClassPathResource outputResource = new ClassPathResource("expectations/UserInfoResource.json");
        String userInfoResource = StringUtils.deleteWhitespace(new String(Files.readAllBytes(outputResource.getFile().toPath())));

        SearchUserDto searchUserDto = new SearchUserDto();
        searchUserDto.setFiscalCode("NLLGPJ67L30L783W");
        searchUserDto.setStatuses(List.of(RelationshipState.ACTIVE));

        when(userService.getUserInfoV2(searchUserDto.getFiscalCode(), searchUserDto.getStatuses())).thenReturn(userInfoWrapper);

        mockMvc.perform(post(BASE_URL)
                        .content(objectMapper.writeValueAsString(searchUserDto))
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().string(userInfoResource))
                .andExpect(jsonPath("$.onboardedInstitutions", hasSize(2)))
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andReturn();
    }

    @Test
    void getUserInfoNotFound() throws Exception {

        SearchUserDto searchUserDto = new SearchUserDto();
        searchUserDto.setFiscalCode("NLLGPJ67L30L783W");
        searchUserDto.setStatuses(List.of(RelationshipState.ACTIVE));

        when(userService.getUserInfoV2(searchUserDto.getFiscalCode(), searchUserDto.getStatuses())).thenReturn(null);

        mockMvc.perform(post(BASE_URL)
                        .content(objectMapper.writeValueAsString(searchUserDto))
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().string(nullValue()))
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andReturn();
    }

    @Test
    void getUserInfoWithoutFiscalCode() throws Exception {

        SearchUserDto searchUserDto = new SearchUserDto();
        searchUserDto.setStatuses(List.of(RelationshipState.ACTIVE));

        mockMvc.perform(post(BASE_URL)
                        .content(objectMapper.writeValueAsString(searchUserDto))
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void getUserProductInfoOk() throws Exception {

        String institutionId = "institutionId";
        String productId = "productId";
        String userId = "userId";
        UserDetailsWrapper userDetailsWrapper = new UserDetailsWrapper();
        userDetailsWrapper.setUserId(userId);
        userDetailsWrapper.setInstitutionId(institutionId);
        userDetailsWrapper.setProductDetails(buildProductDetails());

        ClassPathResource outputResource = new ClassPathResource("expectations/UserDetailsResource.json");
        String expectedResource = StringUtils.deleteWhitespace(new String(Files.readAllBytes(outputResource.getFile().toPath())));

        when(userService.getUserOnboardedProductsDetailsV2(anyString(), anyString(), anyString())).thenReturn(userDetailsWrapper);

        mockMvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{id}/onboarded-product", userId)
                        .queryParam("institutionId", institutionId)
                        .queryParam("productId", productId)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResource))
                .andReturn();
    }

    @Test
    void getUserProductInfoNotFound() throws Exception {

        String institutionId = "institutionId";
        String productId = "productId";
        String userId = "userId";

        when(userService.getUserOnboardedProductsDetailsV2(userId, institutionId, productId)).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{id}/onboarded-product", userId)
                        .queryParam("institutionId", institutionId)
                        .queryParam("productId", productId)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().string(nullValue()))
                .andReturn();
    }

    @Test
    void getUserProductInfoOkWithoutProductId() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{id}/onboarded-product", "")
                        .queryParam("institutionId", "institutionId")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void getUserProductInfoOkWithoutInstitutionId() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{id}/onboarded-product", "")
                        .queryParam("productId", "prod-io")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    private ProductDetails buildProductDetails() {
        ProductDetails product = new ProductDetails();
        product.setProductId("productId");
        product.setRoles(List.of("role"));
        product.setRole(PartyRole.MANAGER);
        product.setCreatedAt(OffsetDateTime.parse("2024-04-17T01:00:00Z"));
        return product;
    }

}
