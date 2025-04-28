package it.pagopa.selfcare.external_api.service;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.OnboardingResponse;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.OnboardingsResponse;
import it.pagopa.selfcare.external_api.client.MsCoreInstitutionApiClient;
import it.pagopa.selfcare.external_api.client.MsOnboardingTokenControllerApi;
import it.pagopa.selfcare.external_api.connector.FileStorageConnector;
import it.pagopa.selfcare.external_api.exception.ResourceNotFoundException;
import it.pagopa.selfcare.external_api.mapper.InstitutionMapperImpl;
import it.pagopa.selfcare.external_api.mapper.TokenMapperImpl;
import it.pagopa.selfcare.external_api.model.document.ResourceResponse;
import it.pagopa.selfcare.external_api.model.onboarding.InstitutionOnboarding;
import it.pagopa.selfcare.external_api.model.token.Token;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import static com.azure.core.http.ContentType.APPLICATION_OCTET_STREAM;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class ContractServiceImplTest extends BaseServiceTestUtils {
    @InjectMocks
    private ContractServiceImpl contractService;

    @Mock
    private FileStorageConnector storageConnectorMock;

    @Mock
    private MsOnboardingTokenControllerApi tokenControllerApi;

    @Mock
    private MsCoreInstitutionApiClient institutionApiClient;

    @Spy
    private InstitutionMapperImpl institutionMapper;

    @Spy
    private TokenMapperImpl tokenMapper;

    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    void getContractV2() throws Exception {
        InstitutionOnboarding institutionOnboarding = new InstitutionOnboarding();
        institutionOnboarding.setTokenId("tokenId");
        ClassPathResource inputResource = new ClassPathResource("expectations/Token.json");
        byte[] tokenStream = Files.readAllBytes(inputResource.getFile().toPath());
        Token token = objectMapper.readValue(tokenStream, Token.class);
        OnboardingsResponse onboardingsResponse = mock(OnboardingsResponse.class);

        Resource resource = new ByteArrayResource("test content".getBytes());
        ResponseEntity<Resource> responseFile = ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_TYPE, APPLICATION_OCTET_STREAM)
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"contractSigned\"")
          .body(resource);

        when(onboardingsResponse.getOnboardings()).thenReturn(List.of(TestUtils.mockInstance(new OnboardingResponse())));
        when(tokenControllerApi._getContractSigned(anyString())).thenReturn(responseFile);

        it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.TokenResponse tokenResponse = new it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.TokenResponse();
        tokenResponse.setContractSigned(token.getContractSigned());
        when(tokenControllerApi._getToken(any()))
                .thenReturn(ResponseEntity.ok(List.of(tokenResponse)));
        when(institutionApiClient._getOnboardingsInstitutionUsingGET("institutionId", "productId")).thenReturn(ResponseEntity.ok(onboardingsResponse));
        ResourceResponse result = contractService.getContractV2("institutionId", "productId");
        Assertions.assertEquals("application/octet-stream", result.getMimetype());
        Assertions.assertEquals("contractSigned", result.getFileName());
        Assertions.assertEquals(12, result.getData().length);
    }

    @Test
    void getContractV2WhereTokenIsEmpty() {
        InstitutionOnboarding institutionOnboarding = new InstitutionOnboarding();
        institutionOnboarding.setTokenId(null);
        OnboardingsResponse onboardingsResponse = mock(OnboardingsResponse.class);
        when(onboardingsResponse.getOnboardings()).thenReturn(Collections.emptyList());
        when(institutionApiClient._getOnboardingsInstitutionUsingGET("institutionId", "productId")).thenReturn(ResponseEntity.ok(onboardingsResponse));
        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> contractService.getContractV2("institutionId", "productId"),
                "Token for institutionId and productId not found!");
    }

    @Test
    void getContractV2WhereTokenIsNull() {
        InstitutionOnboarding institutionOnboarding = new InstitutionOnboarding();
        institutionOnboarding.setTokenId(null);
        OnboardingsResponse onboardingsResponse = new OnboardingsResponse();
        when(institutionApiClient._getOnboardingsInstitutionUsingGET("institutionId", "productId")).thenReturn(ResponseEntity.ok(onboardingsResponse));
        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> contractService.getContractV2("institutionId", "productId"),
                "Token for institutionId and productId not found!");
    }

    @Test
    void getContractV2WhereContractSignedIsNull() throws IOException {
        InstitutionOnboarding institutionOnboarding = new InstitutionOnboarding();
        institutionOnboarding.setTokenId(null);
        ClassPathResource inputResource = new ClassPathResource("expectations/Token.json");
        byte[] tokenStream = Files.readAllBytes(inputResource.getFile().toPath());
        Token token = objectMapper.readValue(tokenStream, Token.class);
        token.setContractSigned(null);
        OnboardingsResponse onboardingsResponse = mock(OnboardingsResponse.class);

        it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.TokenResponse tokenResponse = new it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.TokenResponse();
        when(tokenControllerApi._getToken(any()))
                .thenReturn(ResponseEntity.ok(List.of(tokenResponse)));

        when(onboardingsResponse.getOnboardings()).thenReturn(List.of(TestUtils.mockInstance(new OnboardingResponse())));
        when(institutionApiClient._getOnboardingsInstitutionUsingGET("institutionId", "productId")).thenReturn(ResponseEntity.ok(onboardingsResponse));
        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> contractService.getContractV2("institutionId", "productId"),
                "Token for institutionId and productId found but contract signed reference is empty!");
    }
}
