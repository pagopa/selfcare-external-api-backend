package it.pagopa.selfcare.external_api.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.external_api.core.InstitutionService;
import it.pagopa.selfcare.external_api.model.institutions.GeographicTaxonomy;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.web.model.mapper.InstitutionResourceMapperImpl;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class InstitutionControllerTest extends BaseControllerTest{

    @InjectMocks
    private InstitutionController institutionController;

    @Spy
    private InstitutionResourceMapperImpl institutionResourceMapper;

    @Mock
    private InstitutionService institutionService;

    @BeforeEach
    void setUp(){
        super.setUp(institutionController);
    }



    @Test
    void getInstitutionGeographicTaxonomiesWith2Elements() throws Exception {

        GeographicTaxonomy geographicTaxonomy = new GeographicTaxonomy();
        geographicTaxonomy.setCode("testCode1");
        geographicTaxonomy.setDesc("testDesc1");

        GeographicTaxonomy geographicTaxonomy2 = new GeographicTaxonomy();
        geographicTaxonomy2.setCode("testCode2");
        geographicTaxonomy2.setDesc("testDesc2");

        when(institutionService.getGeographicTaxonomyList(anyString())).thenReturn(List.of(geographicTaxonomy, geographicTaxonomy2));

        mockMvc.perform(get("/v1/institutions/{institutionId}/geographicTaxonomy", "testInstitutionId")
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(content().string("[{\"code\":\"testCode1\",\"desc\":\"testDesc1\"},{\"code\":\"testCode2\",\"desc\":\"testDesc2\"}]"))
                .andReturn();
    }

    @Test
    void getInstitutionGeographicTaxonomiesWithEmptyList() throws Exception {

        when(institutionService.getGeographicTaxonomyList(anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/v1/institutions/{institutionId}/geographicTaxonomy", "testInstitutionId")
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)))
                .andReturn();
    }


    @Test
    void getInstitutionsByGeoTaxonomiesWith2ReturnedElements() throws Exception {

        ClassPathResource productResponse = new ClassPathResource("expectations/Institution.json");
        byte[] institutionStream = Files.readAllBytes(productResponse.getFile().toPath());
        List<Institution> institution = objectMapper.readValue(institutionStream, new TypeReference<>() {
        });

        ClassPathResource outputResource = new ClassPathResource("expectations/InstitutionDetailResource.json");
        String expectedResource = StringUtils.deleteWhitespace(new String(Files.readAllBytes(outputResource.getFile().toPath())));

        when(institutionService.getInstitutionsByGeoTaxonomies(Set.of("testGeoTaxonomies"), null)).thenReturn(institution);

        mockMvc.perform(get("/v1/institutions/byGeoTaxonomies")
                        .param("geoTaxonomies", "testGeoTaxonomies")
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(content().string(expectedResource))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andReturn();
    }


    @Test
    void getInstitutionsByGeoTaxonomiesWithEmptyList() throws Exception {

        when(institutionService.getInstitutionsByGeoTaxonomies(Set.of("testGeoTaxonomies"), null))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/v1/institutions/byGeoTaxonomies")
                        .param("geoTaxonomies", "testGeoTaxonomies")
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andReturn();
    }


    @Test
    void getInstitutionsByGeoTaxonomiesWithoutGeoTaxonomyParam() throws Exception {

        mockMvc.perform(get("/v1/institutions/byGeoTaxonomies")
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn();
    }
}
