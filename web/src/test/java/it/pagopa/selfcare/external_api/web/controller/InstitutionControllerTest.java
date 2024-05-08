package it.pagopa.selfcare.external_api.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.pagopa.selfcare.external_api.core.InstitutionService;
import it.pagopa.selfcare.external_api.model.institutions.GeographicTaxonomy;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.external_api.model.product.Product;
import it.pagopa.selfcare.external_api.model.user.UserInfo;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class InstitutionControllerTest {

    @InjectMocks
    private InstitutionController institutionController;

    @Spy
    private InstitutionResourceMapperImpl institutionResourceMapper;

    @Mock
    private InstitutionService institutionService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        //objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(institutionController)
                .build();
    }

    @Test
    public void getInstitutionsWithOneReturnedElement() throws Exception {

        ClassPathResource inputResource = new ClassPathResource("expectations/InstitutionInfo.json");
        byte[] institutionInfoStream = Files.readAllBytes(inputResource.getFile().toPath());
        List<InstitutionInfo> institutionInfos = objectMapper.readValue(institutionInfoStream, new TypeReference<>() {
        });

        ClassPathResource outputResource = new ClassPathResource("expectations/InstitutionResource.json");
        String expectedResource = StringUtils.deleteWhitespace(new String(Files.readAllBytes(outputResource.getFile().toPath())));

        when(institutionService.getInstitutions(anyString())).thenReturn(institutionInfos);

        mockMvc.perform(get("/v1/institutions")
                        .param("productId", "testProductId")
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(content().string(expectedResource))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andReturn();
    }

    @Test
    public void getInstitutionsWithEmptyList() throws Exception {

        when(institutionService.getInstitutions(anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/v1/institutions")
                        .param("productId", "testProductId")
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andReturn();
    }

    @Test
    public void getInstitutionsWithoutRequiredProductId() throws Exception {

        mockMvc.perform(get("/v1/institutions")
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getInstitutionUserProductWithOneElement() throws Exception {

        ClassPathResource productResponse = new ClassPathResource("expectations/Product.json");
        byte[] productStream = Files.readAllBytes(productResponse.getFile().toPath());
        List<Product> products = objectMapper.readValue(productStream, new TypeReference<>() {
        });

        ClassPathResource outputResource = new ClassPathResource("expectations/ProductResource.json");
        String expectedResource = StringUtils.deleteWhitespace(new String(Files.readAllBytes(outputResource.getFile().toPath())));

        when(institutionService.getInstitutionUserProducts(anyString())).thenReturn(products);

        mockMvc.perform(get("/v1/institutions/{institutionId}/products", "testInstitutionId")
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(content().string(expectedResource))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andReturn();
    }

    @Test
    public void getInstitutionUserProductWithEmptyList() throws Exception {

        when(institutionService.getInstitutionUserProducts(anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/v1/institutions/{institutionId}/products", "testInstitutionId")
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andReturn();
    }

    @Test
    public void getInstitutionProductsUsersWith2ReturnedElements() throws Exception {

        ClassPathResource productResponse = new ClassPathResource("expectations/UserInfo.json");
        byte[] userInfoStream = Files.readAllBytes(productResponse.getFile().toPath());
        List<UserInfo> userInfo = objectMapper.readValue(userInfoStream, new TypeReference<>() {
        });

        ClassPathResource outputResource = new ClassPathResource("expectations/UserResource.json");
        String expectedResource = StringUtils.deleteWhitespace(new String(Files.readAllBytes(outputResource.getFile().toPath())));

        when(institutionService.getInstitutionProductUsers(any(), any(), any(), any(), any())).thenReturn(userInfo);

        mockMvc.perform(get("/v1/institutions/{institutionId}/products/{productId}/users", "testInstitutionId", "testProductId")
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(content().string(expectedResource))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andReturn();
    }

    @Test
    public void getInstitutionProductsUsersWithEmptyList() throws Exception {

        when(institutionService.getInstitutionProductUsers("testInstitutionId", "testProductId", java.util.Optional.empty(), java.util.Optional.empty(), null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/v1/institutions/{institutionId}/products/{productId}/users", "testInstitutionId", "testProductId")
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andReturn();
    }


    @Test
    public void getInstitutionGeographicTaxonomiesWith2Elements() throws Exception {

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
    public void getInstitutionGeographicTaxonomiesWithEmptyList() throws Exception {

        when(institutionService.getGeographicTaxonomyList(anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/v1/institutions/{institutionId}/geographicTaxonomy", "testInstitutionId")
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)))
                .andReturn();
    }


    @Test
    public void getInstitutionsByGeoTaxonomiesWith2ReturnedElements() throws Exception {

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
    public void getInstitutionsByGeoTaxonomiesWithEmptyList() throws Exception {

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
    public void getInstitutionsByGeoTaxonomiesWithoutGeoTaxonomyParam() throws Exception {

        mockMvc.perform(get("/v1/institutions/byGeoTaxonomies")
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn();
    }
}
