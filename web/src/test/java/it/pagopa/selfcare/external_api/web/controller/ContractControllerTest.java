package it.pagopa.selfcare.external_api.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.external_api.core.ContractService;
import it.pagopa.selfcare.external_api.model.documents.ResourceResponse;
import it.pagopa.selfcare.external_api.web.config.WebTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = {ContractController.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {ContractController.class, WebTestConfig.class})
class ContractControllerTest {
    private static final String BASE_PATH = "/contracts";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper objectMapper;

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
        when(contractService.getContract(any(), any())).thenReturn(response);
        //when
        mvc.perform(MockMvcRequestBuilders.get(BASE_PATH + "/{institutionId}", institutionId)
                        .param("productId", productId)
                        .accept(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s", response.getFileName())))
                .andExpect(content().bytes(response.getData()));
        //then
        verify(contractService, times(1)).getContract(institutionId, productId);
        verifyNoMoreInteractions(contractService);

    }
}