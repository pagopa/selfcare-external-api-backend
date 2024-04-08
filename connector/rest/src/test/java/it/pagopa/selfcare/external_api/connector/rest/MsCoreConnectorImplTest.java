package it.pagopa.selfcare.external_api.connector.rest;

import it.pagopa.selfcare.core.generated.openapi.v1.dto.*;
import it.pagopa.selfcare.external_api.connector.rest.client.MsCoreInstitutionApiClient;
import it.pagopa.selfcare.external_api.connector.rest.client.MsCoreRestClient;
import it.pagopa.selfcare.external_api.connector.rest.client.MsCoreUserApiClient;
import it.pagopa.selfcare.external_api.connector.rest.mapper.InstitutionMapper;
import it.pagopa.selfcare.external_api.connector.rest.mapper.InstitutionMapperImpl;
import it.pagopa.selfcare.external_api.connector.rest.mapper.UserProductMapper;
import it.pagopa.selfcare.external_api.connector.rest.mapper.UserProductMapperImpl;
import it.pagopa.selfcare.external_api.connector.rest.model.pnpg.CreatePnPgInstitutionRequest;
import it.pagopa.selfcare.external_api.connector.rest.model.pnpg.InstitutionPnPgResponse;
import it.pagopa.selfcare.external_api.model.onboarding.InstitutionOnboarding;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionInfo;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionResponse;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingInfoResponse;
import it.pagopa.selfcare.external_api.model.pnpg.CreatePnPgInstitution;
import it.pagopa.selfcare.external_api.model.token.Token;
import it.pagopa.selfcare.external_api.model.token.TokenUser;
import it.pagopa.selfcare.external_api.model.user.RelationshipState;
import it.pagopa.selfcare.external_api.model.user.UserProducts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

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

    @Mock
    private MsCoreInstitutionApiClient institutionApiClient;

    @Mock
    private MsCoreUserApiClient userApiClient;

    @Spy
    InstitutionMapper institutionMapper = new InstitutionMapperImpl();

    @Spy
    UserProductMapper userProductMapper = new UserProductMapperImpl();


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

    @Test
    void getOnboardingInfoWithStatus() {
        //given
        OnboardingInfoResponse response = new OnboardingInfoResponse();
        OnboardedInstitutionResponse institutionResponse = new OnboardedInstitutionResponse();
        institutionResponse.setId("id");
        institutionResponse.setAddress("address");
        response.setInstitutions(List.of(institutionResponse));
        when(msCoreRestClient.getInstitutionProductsInfo(anyString(), any())).thenReturn(response);
        //when
        OnboardingInfoResponse result = msCoreConnector.getInstitutionProductsInfo("userId", List.of(RelationshipState.PENDING));
        //then
        assertEquals(response, result);
        assertNotNull(result.getInstitutions());
        assertEquals(1, result.getInstitutions().size());
        assertEquals("address", result.getInstitutions().get(0).getAddress());
        verify(msCoreRestClient, times(1)).getInstitutionProductsInfo("userId" , new String[]{"PENDING"});
        verifyNoMoreInteractions(msCoreRestClient);
    }

    @Test
    void getOnboardingInfoWithNullStatus() {
        //given
        OnboardingInfoResponse response = new OnboardingInfoResponse();
        OnboardedInstitutionResponse institutionResponse = new OnboardedInstitutionResponse();
        response.setInstitutions(List.of(institutionResponse));
        when(msCoreRestClient.getInstitutionProductsInfo(anyString(), any())).thenReturn(response);
        //when
        OnboardingInfoResponse result = msCoreConnector.getInstitutionProductsInfo("userId", null);
        //then
        assertEquals(response, result);
        assertNotNull(result.getInstitutions());
        assertEquals(1, result.getInstitutions().size());
        verify(msCoreRestClient, times(1)).getInstitutionProductsInfo("userId" , null);
        verifyNoMoreInteractions(msCoreRestClient);
    }

    @Test
    void getInstitutionOnboardings(){
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        OnboardingsResponse onboardingsResponse = new OnboardingsResponse();
        onboardingsResponse.setOnboardings(List.of(new OnboardingResponse()));
        when(institutionApiClient._getOnboardingsInstitutionUsingGET(institutionId, productId))
                .thenReturn(ResponseEntity.of(Optional.of(onboardingsResponse)));
        //when
        InstitutionOnboarding result = msCoreConnector.getInstitutionOnboardings(institutionId, productId);
        //then
        assertNotNull(result);
        verify(institutionApiClient, times(1))._getOnboardingsInstitutionUsingGET(institutionId, productId);
        verifyNoMoreInteractions(institutionApiClient);

    }

    @Test
    void getOnboardedUsers(){
        //given
        var onboardedUsersResponse = new OnboardedUsersResponse();
        var userProductsResponse = new UserProductsResponse();
        userProductsResponse.setId("userId");
        onboardedUsersResponse.users(List.of());
        final String userId = "userId";
        when(userApiClient._getOnboardedUsersUsingGET(List.of(userId)))
                .thenReturn(ResponseEntity.of(Optional.of(onboardedUsersResponse)));
        //when
        TokenUser tokenUser = new TokenUser();
        tokenUser.setUserId(userId);
        List<UserProducts> result = msCoreConnector.getOnboarderUsers(List.of(tokenUser));
        //then
        assertNotNull(result);
        verify(userApiClient, times(1))._getOnboardedUsersUsingGET(List.of(userId));
        verifyNoMoreInteractions(userApiClient);

    }

    @Test
    void getInstitutionDetails(){
        //given
        InstitutionResponse institutionResponse = new InstitutionResponse();
        OnboardedProductResponse onboardedProductResponse = new OnboardedProductResponse();
        onboardedProductResponse.setStatus(OnboardedProductResponse.StatusEnum.ACTIVE);
        institutionResponse.setOnboarding(List.of(onboardedProductResponse));
        institutionResponse.setInstitutionType(InstitutionResponse.InstitutionTypeEnum.PA);
        final String userId = "userId";
        when(institutionApiClient._retrieveInstitutionByIdUsingGET(anyString()))
                .thenReturn(ResponseEntity.of(Optional.of(institutionResponse)));
        //when
        TokenUser tokenUser = new TokenUser();
        tokenUser.setUserId(userId);
        List<OnboardedInstitutionInfo> result = msCoreConnector.getInstitutionDetails("id");
        //then
        assertNotNull(result);
        verify(institutionApiClient, times(1))._retrieveInstitutionByIdUsingGET("id");
        verifyNoMoreInteractions(institutionApiClient);
    }
}
