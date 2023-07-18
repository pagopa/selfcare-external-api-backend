package it.pagopa.selfcare.external_api.connector.rest;

import it.pagopa.selfcare.external_api.connector.rest.client.MsCoreRestClient;
import it.pagopa.selfcare.external_api.connector.rest.model.pnpg.CreatePnPgInstitutionRequest;
import it.pagopa.selfcare.external_api.connector.rest.model.pnpg.InstitutionPnPgResponse;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionResponse;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingInfoResponse;
import it.pagopa.selfcare.external_api.model.pnpg.CreatePnPgInstitution;
import it.pagopa.selfcare.external_api.model.token.Token;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MsCoreConnectorImplTest {

    @InjectMocks
    private MsCoreConnectorImpl msCoreConnector;

    @Mock
    private MsCoreRestClient msCoreRestClient;

    @Test
    void createPnPgInstitution() {
        //given
        CreatePnPgInstitution request = mockInstance(new CreatePnPgInstitution());
        InstitutionPnPgResponse response = mockInstance(new InstitutionPnPgResponse());
        when(msCoreRestClient.createPnPgInstitution(any()))
                .thenReturn(response);
        //when
        String institutionPnPgResponse = msCoreConnector.createPnPgInstitution(request);
        //then
        ArgumentCaptor<CreatePnPgInstitutionRequest> requestCaptor = ArgumentCaptor.forClass(CreatePnPgInstitutionRequest.class);
        verify(msCoreRestClient, times(1))
                .createPnPgInstitution(requestCaptor.capture());
        verifyNoMoreInteractions(msCoreRestClient);
        CreatePnPgInstitutionRequest capturedRequest = requestCaptor.getValue();
        assertEquals(request.getExternalId(), capturedRequest.getTaxId());
        assertEquals(request.getDescription(), capturedRequest.getDescription());

    }

    @Test
    void getToken(){
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        Token token = mockInstance(new Token());
        when(msCoreRestClient.getToken(any(), any())).thenReturn(token);
        //when
        Token result = msCoreConnector.getToken(institutionId, productId);
        //then
        assertEquals(token, result);
        verify(msCoreRestClient, times(1)).getToken(institutionId, productId);
        verifyNoMoreInteractions(msCoreRestClient);

    }

    @Test
    void getOnboardingInfo() {
        //given
        OnboardingInfoResponse response = new OnboardingInfoResponse();
        OnboardedInstitutionResponse institutionResponse = new OnboardedInstitutionResponse();
        institutionResponse.setId("id");
        institutionResponse.setAddress("address");
        response.setInstitutions(List.of(institutionResponse));
        when(msCoreRestClient.getInstitutionProductsInfo(anyString())).thenReturn(response);
        //when
        OnboardingInfoResponse result = msCoreConnector.getInstitutionProductsInfo("userId");
        //then
        assertEquals(response, result);
        assertNotNull(result.getInstitutions());
        assertEquals(1, result.getInstitutions().size());
        assertEquals("address", result.getInstitutions().get(0).getAddress());
        verify(msCoreRestClient, times(1)).getInstitutionProductsInfo("userId");
        verifyNoMoreInteractions(msCoreRestClient);

    }
}
