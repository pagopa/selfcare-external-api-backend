package it.pagopa.selfcare.external_api.connector.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.external_api.connector.rest.client.MsPartyRegistryProxyRestClient;
import it.pagopa.selfcare.external_api.connector.rest.config.BaseConnectorTest;
import it.pagopa.selfcare.external_api.model.institutions.InstitutionResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MsPartyRegistryProxyConnectorImplTest extends BaseConnectorTest {

    @Mock
    private MsPartyRegistryProxyRestClient msPartyRegistryProxyRestClientMock;

    @InjectMocks
    private MsPartyRegistryProxyConnectorImpl msPartyRegistryProxyConnectorImplMock;

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

}
