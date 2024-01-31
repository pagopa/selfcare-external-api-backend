package it.pagopa.selfcare.external_api.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.external_api.core.ContractService;
import it.pagopa.selfcare.external_api.core.InstitutionService;
import it.pagopa.selfcare.external_api.model.documents.ResourceResponse;
import it.pagopa.selfcare.external_api.web.config.WebTestConfig;
import it.pagopa.selfcare.external_api.web.model.mapper.InstitutionResourceMapper;
import it.pagopa.selfcare.external_api.web.model.mapper.InstitutionResourceMapperImpl;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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
}
