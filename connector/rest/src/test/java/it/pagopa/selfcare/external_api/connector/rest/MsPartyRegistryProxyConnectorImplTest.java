package it.pagopa.selfcare.external_api.connector.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.external_api.connector.rest.client.MsPartyRegistryProxyRestClient;
import it.pagopa.selfcare.external_api.connector.rest.client.MsRegistryProxyNationalRegistryRestClient;
import it.pagopa.selfcare.external_api.connector.rest.config.BaseConnectorTest;
import it.pagopa.selfcare.external_api.connector.rest.mapper.RegistryProxyMapperImpl;
import it.pagopa.selfcare.external_api.model.institutions.InstitutionResource;
import it.pagopa.selfcare.external_api.model.nationalRegistries.LegalVerification;
import it.pagopa.selfcare.registry_proxy.generated.openapi.v1.dto.LegalVerificationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MsPartyRegistryProxyConnectorImplTest extends BaseConnectorTest {

    @Mock
    private MsPartyRegistryProxyRestClient msPartyRegistryProxyRestClientMock;

    @Mock
    private MsRegistryProxyNationalRegistryRestClient msRegistryProxyNationalRegistryRestClient;

    @InjectMocks
    private MsPartyRegistryProxyConnectorImpl msPartyRegistryProxyConnectorImplMock;
    @Spy
    private RegistryProxyMapperImpl registryProxyMapper;

    @Test
    void findInstitutionError(){
        assertThrows(IllegalArgumentException.class,
                () -> msPartyRegistryProxyConnectorImplMock.findInstitution(null),
                "An external institution Id is required ");
    }

    @Test
    void findInstitutionOk() throws IOException {

        String externalId = "externalId";

        ClassPathResource resource = new ClassPathResource("stubs/institutionResource.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        InstitutionResource institutionResource = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        when(msPartyRegistryProxyRestClientMock.findInstitution(externalId, null, null)).thenReturn(institutionResource)
                .thenReturn(institutionResource);

        InstitutionResource result = msPartyRegistryProxyConnectorImplMock.findInstitution(externalId);

        assertEquals(institutionResource, result);
        verify(msPartyRegistryProxyRestClientMock, times(1)).findInstitution(externalId, null, null);


    }

    @Test
    void verifyLegal(){
        //given
        final String taxId = "taxId";
        final String vatNumber = "vatNumber";

        when(msRegistryProxyNationalRegistryRestClient._verifyLegalUsingGET(anyString(), anyString())).thenReturn(new ResponseEntity<>( new LegalVerificationResult(), HttpStatus.OK));

        //when
        LegalVerification result = msPartyRegistryProxyConnectorImplMock.verifyLegal(taxId, vatNumber);

        //then
        assertNotNull(result);
        verify(msRegistryProxyNationalRegistryRestClient, times(1))._verifyLegalUsingGET(taxId, vatNumber);

    }

}
