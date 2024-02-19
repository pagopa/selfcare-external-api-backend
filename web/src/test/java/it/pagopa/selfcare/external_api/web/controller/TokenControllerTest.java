package it.pagopa.selfcare.external_api.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.external_api.core.TokenService;
import it.pagopa.selfcare.external_api.model.token.ProductToken;
import it.pagopa.selfcare.external_api.model.token.Token;
import it.pagopa.selfcare.external_api.model.token.TokenOnboardedUsers;
import it.pagopa.selfcare.external_api.model.user.InstitutionProducts;
import it.pagopa.selfcare.external_api.model.user.UserProducts;
import it.pagopa.selfcare.external_api.web.config.WebTestConfig;
import it.pagopa.selfcare.external_api.web.model.mapper.TokenResourceMapperImpl;
import it.pagopa.selfcare.external_api.web.model.tokens.TokensResource;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = {TokenController.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {TokenController.class, WebTestConfig.class, TokenResourceMapperImpl.class})
class TokenControllerTest {

    private static final String BASE_URL = "/v1/tokens";

    @Autowired
    protected MockMvc mvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @MockBean
    private TokenService tokenService;

    @Test
    void getTokensByProduct() throws Exception {
        //given
        final String productId = "productId";
        TokenOnboardedUsers token = new TokenOnboardedUsers();
        token.setId("id");
        token.setProductId(productId);
        token.setInstitutionId("institutionId");

        UserProducts userProduct = new UserProducts();
        // Item ignored because institutionId is empty
        InstitutionProducts institutionProductsIgnored = new InstitutionProducts();

        InstitutionProducts institutionProducts = new InstitutionProducts();
        ProductToken productToken = new ProductToken();
        productToken.setProductId(productId);
        institutionProducts.setInstitutionId(token.getInstitutionId());
        institutionProducts.setProducts(List.of(productToken));
        userProduct.setBindings(List.of(institutionProductsIgnored, institutionProducts));
        token.setOnboardedUsers(List.of(userProduct));

        when(tokenService.findByProductId("productId", 1, 10))
                .thenReturn(List.of(token));
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/products/{productId}", productId )
                        .queryParam("page", "1")
                        .queryParam("size", "10")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
        //then
        TokensResource response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertNotNull(response);
        assertNotNull(response.getItems());
        assertEquals(1, response.getItems().size());
    }
}
