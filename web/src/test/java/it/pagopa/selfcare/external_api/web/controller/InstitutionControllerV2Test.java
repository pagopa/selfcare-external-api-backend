package it.pagopa.selfcare.external_api.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.external_api.core.ContractService;
import it.pagopa.selfcare.external_api.core.InstitutionService;
import it.pagopa.selfcare.external_api.model.documents.ResourceResponse;
import it.pagopa.selfcare.external_api.model.product.Product;
import it.pagopa.selfcare.external_api.web.config.WebTestConfig;
import it.pagopa.selfcare.external_api.web.model.mapper.InstitutionResourceMapper;
import it.pagopa.selfcare.external_api.web.model.mapper.InstitutionResourceMapperImpl;
import it.pagopa.selfcare.external_api.web.model.products.ProductResource;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;
import java.util.List;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = {InstitutionV2Controller.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {InstitutionV2Controller.class, WebTestConfig.class, InstitutionResourceMapperImpl.class})
public class InstitutionControllerV2Test {

    private static final String BASE_URL = "/v2/institutions";

    @InjectMocks
    private InstitutionV2Controller institutionController;

    @Autowired
    private ObjectMapper objectMapper;

    @Spy
    private InstitutionResourceMapper institutionResourceMapper = new InstitutionResourceMapperImpl();

    @MockBean
    private InstitutionService institutionServiceMock;

    @Autowired
    protected MockMvc mvc;

    @MockBean
    private ContractService contractService;

    @Test
    void getContract() throws Exception{
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        ResourceResponse response = mockInstance(new ResourceResponse());
        byte[] mockData = "mock".getBytes();
        response.setData(mockData);
        when(contractService.getContractV2(institutionId, productId)).thenReturn(response);
        //when
        mvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/{institutionId}/contract", institutionId)
                        .param("productId", productId)
                        .accept(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s", response.getFileName())))
                .andExpect(content().bytes(response.getData()));
        //then
        verify(contractService, times(1)).getContractV2(institutionId, productId);
        verifyNoMoreInteractions(contractService);
    }

    /**
     * Method under test: {@link InstitutionV2Controller#getInstitutionUserProducts(String)}
     */
    @Test
    void getInstitutionUserProducts() throws Exception {
        //given
        final String institutionId = "institutionId";
        Product product = mockInstance(new Product());
        when(institutionServiceMock.getInstitutionUserProductsV2(anyString()))
                .thenReturn(Collections.singletonList(product));
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/" + institutionId + "/products")
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
        verify(institutionServiceMock, times(1))
                .getInstitutionUserProductsV2(institutionId);
        verifyNoMoreInteractions(institutionServiceMock);
    }
}
