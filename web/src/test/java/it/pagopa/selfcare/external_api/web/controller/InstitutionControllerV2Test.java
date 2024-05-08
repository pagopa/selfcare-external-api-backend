package it.pagopa.selfcare.external_api.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.external_api.core.ContractService;
import it.pagopa.selfcare.external_api.core.InstitutionService;
import it.pagopa.selfcare.external_api.core.UserService;
import it.pagopa.selfcare.external_api.model.documents.ResourceResponse;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionInfo;
import it.pagopa.selfcare.external_api.model.product.Product;
import it.pagopa.selfcare.external_api.model.user.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class InstitutionControllerV2Test {

    private static final String BASE_URL = "/v2/institutions";

    @InjectMocks

    private InstitutionV2Controller institutionV2Controller;
    @Mock
    private InstitutionService institutionService;

    @Mock
    private UserService userService;

    @Mock
    private ContractService contractService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(institutionV2Controller)
                .build();
    }

    @Test
    public void getInstitutionsWith2Elements() throws Exception {

        ClassPathResource inputResource = new ClassPathResource("expectations/OnboardedInstitutionInfo.json");
        byte[] institutionInfoStream = Files.readAllBytes(inputResource.getFile().toPath());
        List<OnboardedInstitutionInfo> onboardedInstitutionInfos = objectMapper.readValue(institutionInfoStream, new TypeReference<>() {});
        onboardedInstitutionInfos.forEach(onboardedInstitutionInfo -> onboardedInstitutionInfo.setState("ACTIVE"));

        ClassPathResource outputResource = new ClassPathResource("expectations/InstitutionResourceV2.json");
        String expectedResource = StringUtils.deleteWhitespace(new String(Files.readAllBytes(outputResource.getFile().toPath())));

        SelfCareUser selfCareUser = SelfCareUser.builder("id").name("nome").surname("cognome").build();
        Authentication authentication = mock(Authentication.class);

        when(authentication.getPrincipal()).thenReturn(selfCareUser);
        final String productId = "productId";

        when(userService.getOnboardedInstitutionsDetailsActive(anyString(), anyString()))
                .thenReturn(onboardedInstitutionInfos);
        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL)
                        .param("productId", productId)
                        .principal(authentication)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(content().string(expectedResource))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(status().isOk())
                .andReturn();
        //then

        verify(userService, times(1))
                .getOnboardedInstitutionsDetailsActive(selfCareUser.getId(), productId);
        verifyNoMoreInteractions(institutionService);
    }

    @Test
    public void getInstitutionsWith1Elements() throws Exception {

        ClassPathResource inputResource = new ClassPathResource("expectations/OnboardedInstitutionInfo.json");
        byte[] institutionInfoStream = Files.readAllBytes(inputResource.getFile().toPath());
        List<OnboardedInstitutionInfo> onboardedInstitutionInfos = objectMapper.readValue(institutionInfoStream, new TypeReference<>() {});

        ClassPathResource outputResource = new ClassPathResource("expectations/InstitutionResourceV2.json");
        String expectedResource = StringUtils.deleteWhitespace(new String(Files.readAllBytes(outputResource.getFile().toPath())));

        SelfCareUser selfCareUser = SelfCareUser.builder("id").name("nome").surname("cognome").build();
        Authentication authentication = mock(Authentication.class);

        when(authentication.getPrincipal()).thenReturn(selfCareUser);
        final String productId = "productId";

        when(userService.getOnboardedInstitutionsDetailsActive(anyString(), anyString()))
                .thenReturn(onboardedInstitutionInfos);
        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL)
                        .param("productId", productId)
                        .principal(authentication)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(content().string(expectedResource))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(status().isOk())
                .andReturn();
        //then

        verify(userService, times(1))
                .getOnboardedInstitutionsDetailsActive(selfCareUser.getId(), productId);
        verifyNoMoreInteractions(institutionService);
    }

    @Test
    public void getInstitutionsWithoutElement() throws Exception {

        SelfCareUser selfCareUser = SelfCareUser.builder("id").name("nome").surname("cognome").build();
        Authentication authentication = mock(Authentication.class);

        when(authentication.getPrincipal()).thenReturn(selfCareUser);
        final String productId = "productId";

        when(userService.getOnboardedInstitutionsDetailsActive(anyString(), anyString()))
                .thenReturn(Collections.emptyList());
        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL)
                        .param("productId", productId)
                        .principal(authentication)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(status().isOk())
                .andReturn();
        //then

        verify(userService, times(1))
                .getOnboardedInstitutionsDetailsActive(selfCareUser.getId(), productId);
        verifyNoMoreInteractions(institutionService);
    }

    @Test
    public void getInstitutionsWithoutProductId() throws Exception {

        SelfCareUser selfCareUser = SelfCareUser.builder("id").name("nome").surname("cognome").build();
        Authentication authentication = mock(Authentication.class);

        when(authentication.getPrincipal()).thenReturn(selfCareUser);
        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL)
                        .principal(authentication)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void getContractOk() throws Exception {

        String institutionId = "institutionId";
        String productId = "productId";

        ResourceResponse resourceResponse = new ResourceResponse();
        resourceResponse.setData("data".getBytes());
        resourceResponse.setFileName("fileName");
        resourceResponse.setMimetype("mimetype");
        when(contractService.getContractV2(institutionId, productId)).thenReturn(resourceResponse);

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/{institutionId}/contract", institutionId)
                .param("productId", productId).accept(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s", resourceResponse.getFileName())))
                .andExpect(content().bytes(resourceResponse.getData()));
    }

    @Test
    void getContractOkWithoutProductId() throws Exception {

        String institutionId = "institutionId";

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/{institutionId}/contract", institutionId)
                .accept(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getInstitutionUserProductsWith2Elements() throws Exception {
        ClassPathResource inputResource = new ClassPathResource("expectations/Product.json");
        byte[] productStream = Files.readAllBytes(inputResource.getFile().toPath());
        List<Product> products = objectMapper.readValue(productStream, new TypeReference<>() {});

        ClassPathResource outputResource = new ClassPathResource("expectations/ProductResource.json");
        String expectedResource = StringUtils.deleteWhitespace(new String(Files.readAllBytes(outputResource.getFile().toPath())));

        when(institutionService.getInstitutionUserProductsV2(anyString())).thenReturn(products);

        mockMvc.perform(get("/v2/institutions/{institutionId}/products", "testInstitutionId")
                .contentType(APPLICATION_JSON_VALUE).accept(APPLICATION_JSON_VALUE))
                .andExpect(content().string(expectedResource))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andReturn();
    }

    @Test
    public void getInstitutionUserProductsWithEmptyList() throws Exception {

        when(institutionService.getInstitutionUserProductsV2("testInstitutionId")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/v2/institutions/{institutionId}/products", "testInstitutionId")
                .contentType(APPLICATION_JSON_VALUE).accept(APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andReturn();
    }

    @Test
    public void getInstitutionProductsUsersWith2ReturnedElements() throws Exception {

        ClassPathResource productResponse = new ClassPathResource("expectations/UserInfo.json");
        byte[] userInfoStream = Files.readAllBytes(productResponse.getFile().toPath());
        UserInfo userInfo = objectMapper.readValue(userInfoStream, UserInfo.class);

        ClassPathResource outputResource = new ClassPathResource("expectations/UserResource.json");
        String expectedResource = StringUtils.deleteWhitespace(new String(Files.readAllBytes(outputResource.getFile().toPath())));

        when(institutionService.getInstitutionProductUsers(any(), any(), any(), any(), any())).thenReturn(Collections.singletonList(userInfo));

        mockMvc.perform(get("/v2/institutions/{institutionId}/products/{productId}/users", "testInstitutionId", "testProductId")
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

        mockMvc.perform(get("/v2/institutions/{institutionId}/products/{productId}/users", "testInstitutionId", "testProductId")
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andReturn();
    }
}
