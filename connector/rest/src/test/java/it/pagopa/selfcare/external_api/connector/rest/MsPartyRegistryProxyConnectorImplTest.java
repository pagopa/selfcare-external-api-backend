package it.pagopa.selfcare.external_api.connector.rest;

import it.pagopa.selfcare.external_api.connector.rest.client.MsPartyRegistryProxyRestClient;
import it.pagopa.selfcare.external_api.connector.rest.model.institution.InstitutionResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MsPartyRegistryProxyConnectorImplTest {

    @Mock
    private MsPartyRegistryProxyRestClient msPartyRegistryProxyRestClientMock;

    @InjectMocks
    private MsPartyRegistryProxyConnectorImpl msPartyRegistryProxyConnectorImplMock;

    private static final String EXTERNAL_INSTITUTION_ID_IS_REQUIRED = "An external institution Id is required ";

    @Test
    void getInsituttionCategory() {
        // given
        String instiutionExternalIdMock = "externalId";
        InstitutionResource institutionResourceMock = mockInstance(new InstitutionResource(), "setCategory");
        String institutionCategoryMock = "L6";
        institutionResourceMock.setCategory(institutionCategoryMock);
        when(msPartyRegistryProxyRestClientMock.findInstitution(any(), any(), any()))
                .thenReturn(institutionResourceMock);
        // when
        String result = msPartyRegistryProxyConnectorImplMock.getInstitutionCategory(instiutionExternalIdMock);
        // then
        assertEquals(institutionCategoryMock, result);
        verify(msPartyRegistryProxyRestClientMock, times(1))
                .findInstitution(eq(instiutionExternalIdMock), isNull(), isNull());
        verifyNoMoreInteractions(msPartyRegistryProxyRestClientMock);
    }

    @Test
    void getInstitutionCategory_nullInstitutionExternalId() {
        // given
        String instiutionExternalIdMock = null;
        // when
        Executable executable = () -> msPartyRegistryProxyConnectorImplMock.getInstitutionCategory(instiutionExternalIdMock);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(EXTERNAL_INSTITUTION_ID_IS_REQUIRED, e.getMessage());
        verifyNoInteractions(msPartyRegistryProxyRestClientMock);
    }

}
