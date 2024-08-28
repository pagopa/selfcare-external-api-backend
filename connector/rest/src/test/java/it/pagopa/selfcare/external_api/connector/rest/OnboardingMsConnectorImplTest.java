package it.pagopa.selfcare.external_api.connector.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.InstitutionResponse;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.InstitutionsResponse;
import it.pagopa.selfcare.external_api.connector.rest.client.MsCoreInstitutionApiClient;
import it.pagopa.selfcare.external_api.connector.rest.client.MsOnboardingControllerApi;
import it.pagopa.selfcare.external_api.connector.rest.client.MsOnboardingTokenControllerApi;
import it.pagopa.selfcare.external_api.connector.rest.config.BaseConnectorTest;
import it.pagopa.selfcare.external_api.connector.rest.mapper.OnboardingMapperImpl;
import it.pagopa.selfcare.external_api.connector.rest.mapper.TokenMapperImpl;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.onboarding.*;
import it.pagopa.selfcare.external_api.model.token.Token;
import it.pagopa.selfcare.external_api.model.token.TokenOnboardedUsers;
import it.pagopa.selfcare.onboarding.common.OnboardingStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OnboardingMsConnectorImplTest extends BaseConnectorTest {

    @InjectMocks
    private OnboardingMsConnectorImpl onboardingMsConnector;

    @Mock
    private MsOnboardingTokenControllerApi tokenControllerApi;

    @Mock
    private MsOnboardingControllerApi onboardingControllerApi;

    @Mock
    private MsCoreInstitutionApiClient institutionApiClient;

    @Spy
    private TokenMapperImpl tokenMapper;

    @Spy
    private OnboardingMapperImpl onboardingMapper;

    @Test
    void getToken() throws IOException {
        final String onboardingId = "onboardingId";

        ClassPathResource resource = new ClassPathResource("stubs/TokenResponse.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.TokenResponse tokenResponse = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        ClassPathResource expectationResource = new ClassPathResource("stubs/Token.json");
        byte[] expectationResourceStream = Files.readAllBytes(expectationResource.getFile().toPath());
        List<Token> expectation = objectMapper.readValue(expectationResourceStream, new TypeReference<>() {
        });

        when(tokenControllerApi._v1TokensGet(onboardingId))
                .thenReturn(ResponseEntity.ok(List.of(tokenResponse)));

        List<Token> response = onboardingMsConnector.getToken(onboardingId);


        Assertions.assertEquals(1, response.size());
        Assertions.assertEquals(objectMapper.writeValueAsString(expectation), objectMapper.writeValueAsString(response));

        verify(tokenControllerApi, times(1))
                ._v1TokensGet(onboardingId);
        verifyNoMoreInteractions(tokenControllerApi);
    }

    @Test
    void getTokenEmptyList(){

        final String onboardingId = "onboardingId";
        when(tokenControllerApi._v1TokensGet(onboardingId))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        List<Token> response = onboardingMsConnector.getToken(onboardingId);

        Assertions.assertEquals(0, response.size());
        verify(tokenControllerApi, times(1))
                ._v1TokensGet(onboardingId);
        verifyNoMoreInteractions(tokenControllerApi);

    }

    @Test
    void onboarding_institutionDefault() {
        // given
        OnboardingData onboardingData = new OnboardingData();
        onboardingData.setTaxCode("taxCode");
        onboardingData.setInstitutionType(InstitutionType.GSP);
        Billing billing = mockInstance(new Billing());
        InstitutionUpdate institutionUpdate = new InstitutionUpdate();
        institutionUpdate.setTaxCode("taxCode");
        institutionUpdate.setDescription("description");
        onboardingData.setBilling(billing);
        onboardingData.setUsers(List.of(mockInstance(new User())));
        onboardingData.setInstitutionUpdate(institutionUpdate);

        onboardingMsConnector.onboarding(onboardingData);

        ArgumentCaptor<OnboardingDefaultRequest> onboardingRequestCaptor = ArgumentCaptor.forClass(OnboardingDefaultRequest.class);
        verify(onboardingControllerApi, times(1))
                ._v1OnboardingCompletionPost(onboardingRequestCaptor.capture());
        OnboardingDefaultRequest actual = onboardingRequestCaptor.getValue();
        assertEquals(actual.getInstitution().getTaxCode(), institutionUpdate.getTaxCode());
        assertEquals(actual.getInstitution().getDescription(), institutionUpdate.getDescription());
        verifyNoMoreInteractions(onboardingControllerApi);
    }

    @Test
    void onboarding_institutionPa() {

        OnboardingData onboardingData = new OnboardingData();
        onboardingData.setTaxCode("taxCode");
        onboardingData.setInstitutionType(InstitutionType.PA);
        Billing billing = mockInstance(new Billing());
        InstitutionUpdate institutionUpdate = new InstitutionUpdate();
        institutionUpdate.setTaxCode("taxCode");
        onboardingData.setBilling(billing);
        onboardingData.setUsers(List.of(mockInstance(new User())));
        onboardingData.setInstitutionUpdate(institutionUpdate);

        onboardingMsConnector.onboarding(onboardingData);

        ArgumentCaptor<OnboardingPaRequest> onboardingRequestCaptor = ArgumentCaptor.forClass(OnboardingPaRequest.class);
        verify(onboardingControllerApi, times(1))
                ._v1OnboardingPaCompletionPost(onboardingRequestCaptor.capture());
        OnboardingPaRequest actual = onboardingRequestCaptor.getValue();
        assertEquals(actual.getInstitution().getTaxCode(), institutionUpdate.getTaxCode());
        verifyNoMoreInteractions(onboardingControllerApi);
    }
    @Test
    void onboarding_institutionPsp() {

        OnboardingData onboardingData = new OnboardingData();
        onboardingData.setTaxCode("taxCode");
        onboardingData.setInstitutionType(InstitutionType.PSP);
        Billing billing = mockInstance(new Billing());
        InstitutionUpdate institutionUpdate = new InstitutionUpdate();
        institutionUpdate.setTaxCode("taxCode");
        institutionUpdate.setPaymentServiceProvider(new PaymentServiceProvider());
        institutionUpdate.setDataProtectionOfficer(new DataProtectionOfficer());
        onboardingData.setBilling(billing);
        onboardingData.setUsers(List.of(mockInstance(new User())));
        onboardingData.setInstitutionUpdate(institutionUpdate);

        onboardingMsConnector.onboarding(onboardingData);

        ArgumentCaptor<OnboardingPspRequest> onboardingRequestCaptor = ArgumentCaptor.forClass(OnboardingPspRequest.class);
        verify(onboardingControllerApi, times(1))
                ._v1OnboardingPspCompletionPost(onboardingRequestCaptor.capture());
        OnboardingPspRequest actual = onboardingRequestCaptor.getValue();
        assertEquals(actual.getInstitution().getTaxCode(), institutionUpdate.getTaxCode());
        assertNotNull(actual.getInstitution().getPaymentServiceProvider());
        assertNotNull(actual.getInstitution().getDataProtectionOfficer());
        verifyNoMoreInteractions(onboardingControllerApi);
    }

    @Test
    void onboarding_importPa() {

        OnboardingData onboardingData = new OnboardingData();
        onboardingData.setTaxCode("taxCode");
        InstitutionUpdate institutionUpdate = new InstitutionUpdate();
        institutionUpdate.setTaxCode("taxCode");
        onboardingData.setUsers(List.of(mockInstance(new User())));
        onboardingData.setInstitutionUpdate(institutionUpdate);

        onboardingMsConnector.onboardingImportPA(onboardingData);

        ArgumentCaptor<OnboardingImportRequest> onboardingRequestCaptor = ArgumentCaptor.forClass(OnboardingImportRequest.class);
        verify(onboardingControllerApi, times(1))
                ._v1OnboardingPaImportPost(onboardingRequestCaptor.capture());
        OnboardingImportRequest actual = onboardingRequestCaptor.getValue();
        assertEquals(actual.getInstitution().getTaxCode(), institutionUpdate.getTaxCode());
        verifyNoMoreInteractions(onboardingControllerApi);
    }

    @Test
    void getInstitutionsByTaxCodeAndSubunitCode_happyPath() throws Exception {
        String taxCode = "taxCode";
        String subunitCode = "subunitCode";

        ClassPathResource institutionsResponseResource = new ClassPathResource("stubs/institutionsResponse.json");
        byte[] institutionsResponseStream = Files.readAllBytes(institutionsResponseResource.getFile().toPath());
        InstitutionsResponse institutionsResponse = objectMapper.readValue(institutionsResponseStream, new TypeReference<>() {
        });

        ClassPathResource institutionResource = new ClassPathResource("stubs/Institution2.json");
        byte[] institutionStream = Files.readAllBytes(institutionResource.getFile().toPath());
        List<Institution> expected = objectMapper.readValue(institutionStream, new TypeReference<>() {
        });

        when(institutionApiClient._getInstitutionsUsingGET(taxCode, subunitCode, null, null)).thenReturn(new ResponseEntity<>(institutionsResponse, HttpStatus.OK));

        List<Institution> institutions = onboardingMsConnector.getInstitutionsByTaxCodeAndSubunitCode(taxCode, subunitCode);

        assertEquals(expected, institutions);
        assert institutions.size() == 1;

        verify(institutionApiClient, times(1))._getInstitutionsUsingGET(taxCode, subunitCode, null, null);
        verify(onboardingMapper, times(1)).toInstitution(any(InstitutionResponse.class));
    }


    @Test
    void getInstitutionsByTaxCodeAndSubunitCode_emptyList() {
        String taxCode = "taxCode";
        String subunitCode = "subunitCode";

        InstitutionsResponse institutionResponse = new InstitutionsResponse();
        institutionResponse.setInstitutions(Collections.emptyList());

        when(institutionApiClient._getInstitutionsUsingGET(taxCode, subunitCode, null, null)).thenReturn(new ResponseEntity<>(institutionResponse, HttpStatus.OK));

        List<Institution> institutions = onboardingMsConnector.getInstitutionsByTaxCodeAndSubunitCode(taxCode, subunitCode);

        verify(institutionApiClient, times(1))._getInstitutionsUsingGET(taxCode, subunitCode, null, null);
        assert institutions.isEmpty();
    }


    @Test
    void getOnboardingsEmptyList() {
        String productId = "prod-io";

        OnboardingGetResponse onboardingGetResponse = new OnboardingGetResponse();
        onboardingGetResponse.setItems(Collections.emptyList());
        when(onboardingControllerApi._v1OnboardingGet(null, null, null,0, productId, 1, OnboardingStatus.TOBEVALIDATED.name(), null, null, null)).thenReturn(ResponseEntity.ok(onboardingGetResponse));

        List<TokenOnboardedUsers> response = onboardingMsConnector.getOnboardings(productId, 0, 1, OnboardingStatus.TOBEVALIDATED.name());

        Assertions.assertEquals(0, response.size());
    }

    @Test
    void getOnboardings() {
        String productId = "prod-io";

        //JSON
        OnboardingGetResponse onboardingGetResponse = new OnboardingGetResponse();
        onboardingGetResponse.setItems(new ArrayList<>());
        when(onboardingControllerApi._v1OnboardingGet(null, null, null, 0, productId, 1, null, null, null, null)).thenReturn(ResponseEntity.ok(onboardingGetResponse));

        List<TokenOnboardedUsers> response = onboardingMsConnector.getOnboardings(productId, 0, 1, null);

        //JSON
        List<TokenOnboardedUsers> expectation = new ArrayList<>();

        Assertions.assertEquals(expectation, response);
        Assertions.assertEquals(0, response.size());
    }
}
