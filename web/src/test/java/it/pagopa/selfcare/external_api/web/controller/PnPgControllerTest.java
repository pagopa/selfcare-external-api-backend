package it.pagopa.selfcare.external_api.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.external_api.core.InstitutionService;
import it.pagopa.selfcare.external_api.web.model.pnpg.CreatePnPgInstitutionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PnPgControllerTest {
    @Autowired
    protected PnPgController pnPgController;

    @Mock
    private InstitutionService institutionServiceMock;

    private final static String BASE_URL = "/v1/pn-pg";

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(pnPgController)
                .build();
    }

    @Test
    void addInstitutionOk() throws Exception {

        CreatePnPgInstitutionDto dto = buildCreatePnPgInstitutionDto();
        String institutionInternalId = UUID.randomUUID().toString();
        when(institutionServiceMock.addInstitution(any()))
                .thenReturn(institutionInternalId);

        mockMvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL + "/institutions/add")
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(institutionInternalId)));
    }

    @Test
    void addInstitutionWithoutExternalId() throws Exception {
        CreatePnPgInstitutionDto dto = buildCreatePnPgInstitutionDto();
        dto.setExternalId(null);
        mockMvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL + "/institutions/add")
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest());
    }

    private CreatePnPgInstitutionDto buildCreatePnPgInstitutionDto() {
        CreatePnPgInstitutionDto createPnPgInstitutionDto = new CreatePnPgInstitutionDto();
        createPnPgInstitutionDto.setDescription("description");
        createPnPgInstitutionDto.setExternalId("externalId");

        return createPnPgInstitutionDto;
    }
}