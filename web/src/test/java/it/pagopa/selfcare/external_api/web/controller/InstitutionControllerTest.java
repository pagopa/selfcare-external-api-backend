package it.pagopa.selfcare.external_api.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.external_api.core.InstitutionService;
import it.pagopa.selfcare.external_api.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.external_api.model.product.Product;
import it.pagopa.selfcare.external_api.web.config.WebTestConfig;
import it.pagopa.selfcare.external_api.web.model.institutions.InstitutionResource;
import it.pagopa.selfcare.external_api.web.model.products.ProductResource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;
import java.util.List;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = {InstitutionController.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {InstitutionController.class, WebTestConfig.class})
class InstitutionControllerTest {
    private static final String BASE_URL = "/institutions";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    private InstitutionService institutionServiceMock;

    @Test
    void getInstitutions() throws Exception {
        //given
        String productId = "productId";
        InstitutionInfo institutionInfo = mockInstance(new InstitutionInfo(), "setId");
        institutionInfo.setId(randomUUID().toString());
        when(institutionServiceMock.getInstitutions(anyString()))
                .thenReturn(Collections.singletonList(institutionInfo));
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "")
                        .param("productId", productId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
        //then
        List<InstitutionResource> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(institutionInfo.getId(), response.get(0).getId().toString());
        assertEquals(institutionInfo.getExternalId(), response.get(0).getExternalId());
        assertEquals(institutionInfo.getDescription(), response.get(0).getDescription());
        verify(institutionServiceMock, times(1))
                .getInstitutions(productId);
        verifyNoMoreInteractions(institutionServiceMock);
    }
    
    @Test
    void getInstitutionUserProducts() throws Exception {
        //given
        String institutionId = "institutionId";
        Product product = mockInstance(new Product());
        when(institutionServiceMock.getInstitutionUserProducts(anyString()))
                .thenReturn(Collections.singletonList(product));
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/"+institutionId+"/products")
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
        //then
        List<ProductResource> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(product.getId(), response.get(0).getId());
        assertEquals(product.getTitle(), response.get(0).getTitle());
        assertEquals(product.getDescription(), response.get(0).getDescription());
        assertEquals(product.getUrlBO(), response.get(0).getUrlBO());
        assertEquals(product.getUrlPublic(), response.get(0).getUrlPublic());
        
    }
}
