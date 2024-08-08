package it.pagopa.selfcare.external_api.service;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.external_api.client.MsCoreInstitutionApiClient;
import it.pagopa.selfcare.external_api.connector.FileStorageConnector;
import it.pagopa.selfcare.external_api.exception.ResourceNotFoundException;
import it.pagopa.selfcare.external_api.model.document.ResourceResponse;
import it.pagopa.selfcare.external_api.model.onboarding.InstitutionOnboarding;
import it.pagopa.selfcare.external_api.model.token.Token;
import it.pagopa.selfcare.onboarding.generated.openapi.v1.api.TokenControllerApi;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

@ExtendWith({MockitoExtension.class})
class ContractServiceImplTest extends BaseServiceTestUtils {
    @InjectMocks
    private ContractServiceImpl contractService;

    @Mock
    private FileStorageConnector storageConnectorMock;

    @Mock
    private TokenControllerApi tokenControllerApi;

    @Mock
    private MsCoreInstitutionApiClient institutionApiClient;

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
        ResourceResponse response = TestUtils.mockInstance(new ResourceResponse());
        Mockito.when(storageConnectorMock.getFile(token.getContractSigned())).thenReturn(response);
        ResourceResponse result = contractService.getContractV2("institutionId", "productId");
        Assertions.assertEquals(response, result);
    }

    @Test
    void getContractV2WhereTokenIsEmpty() {
        InstitutionOnboarding institutionOnboarding = new InstitutionOnboarding();
        institutionOnboarding.setTokenId(null);
        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> contractService.getContractV2("institutionId", "productId"),
                "Token for institutionId and productId not found!");
    }

    @Test
    void getContractV2WhereTokenIsNull() {
        InstitutionOnboarding institutionOnboarding = new InstitutionOnboarding();
        institutionOnboarding.setTokenId(null);
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
        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> contractService.getContractV2("institutionId", "productId"),
                "Token for institutionId and productId found but contract signed reference is empty!");
    }
}
