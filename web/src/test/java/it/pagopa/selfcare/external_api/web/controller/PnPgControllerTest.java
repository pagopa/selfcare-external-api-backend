package it.pagopa.selfcare.external_api.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.external_api.core.InstitutionService;
import it.pagopa.selfcare.external_api.model.pnpg.CreatePnPgInstitution;
import it.pagopa.selfcare.external_api.web.config.WebTestConfig;
import it.pagopa.selfcare.external_api.web.model.pnpg.CreatePnPgInstitutionDto;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = {PnPgController.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {PnPgController.class, WebTestConfig.class})
class PnPgControllerTest {
    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    private InstitutionService institutionServiceMock;
    private final static String BASE_URL = "/pn-pg";

    @Test
    void addInstitution() throws Exception {
        //given
        CreatePnPgInstitutionDto dto = mockInstance(new CreatePnPgInstitutionDto());
        String institutionInternalId = UUID.randomUUID().toString();
        when(institutionServiceMock.addInstitution(any()))
                .thenReturn(institutionInternalId);
        //when
        mvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL + "/institutions/add")
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(institutionInternalId)));
        //then
        ArgumentCaptor<CreatePnPgInstitution> createInstitutionCaptor = ArgumentCaptor.forClass(CreatePnPgInstitution.class);
        verify(institutionServiceMock, times(1))
                .addInstitution(createInstitutionCaptor.capture());
        CreatePnPgInstitution captured = createInstitutionCaptor.getValue();
        assertEquals(dto.getExternalId(), captured.getExternalId());
        assertEquals(dto.getDescription(), captured.getDescription());
        verifyNoMoreInteractions(institutionServiceMock);
    }
}