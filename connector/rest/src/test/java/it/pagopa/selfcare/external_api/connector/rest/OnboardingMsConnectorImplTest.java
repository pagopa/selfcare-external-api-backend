package it.pagopa.selfcare.external_api.connector.rest;

import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import it.pagopa.selfcare.external_api.connector.rest.client.MsOnboardingControllerApi;
import it.pagopa.selfcare.external_api.connector.rest.client.MsOnboardingTokenControllerApi;
import it.pagopa.selfcare.external_api.connector.rest.mapper.OnboardingMapper;
import it.pagopa.selfcare.external_api.connector.rest.mapper.OnboardingMapperImpl;
import it.pagopa.selfcare.external_api.connector.rest.mapper.TokenMapper;
import it.pagopa.selfcare.external_api.connector.rest.mapper.TokenMapperImpl;
import it.pagopa.selfcare.external_api.model.onboarding.*;
import it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.*;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OnboardingMsConnectorImplTest {

    @InjectMocks
    private OnboardingMsConnectorImpl onboardingMsConnector;

    @Mock
    private MsOnboardingTokenControllerApi tokenControllerApi;

    @Mock
    private MsOnboardingControllerApi onboardingControllerApi;

    @Spy
    private TokenMapper tokenMapper = new TokenMapperImpl();

    @Spy
    private OnboardingMapper onboardingMapper = new OnboardingMapperImpl();

    @Test
    void getToken(){
        //given
        final String onboardingId = "onboardingId";
        when(tokenControllerApi._v1TokensGet(onboardingId))
                .thenReturn(ResponseEntity.of(Optional.of(List.of(new TokenResponse()))));
        //when
        onboardingMsConnector.getToken(onboardingId);
        //then
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
        // when
        onboardingMsConnector.onboarding(onboardingData);
        // then

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
        // given
        OnboardingData onboardingData = new OnboardingData();
        onboardingData.setTaxCode("taxCode");
        onboardingData.setInstitutionType(InstitutionType.PA);
        Billing billing = mockInstance(new Billing());
        InstitutionUpdate institutionUpdate = new InstitutionUpdate();
        institutionUpdate.setTaxCode("taxCode");
        onboardingData.setBilling(billing);
        onboardingData.setUsers(List.of(mockInstance(new User())));
        onboardingData.setInstitutionUpdate(institutionUpdate);
        // when
        onboardingMsConnector.onboarding(onboardingData);
        // then

        ArgumentCaptor<OnboardingPaRequest> onboardingRequestCaptor = ArgumentCaptor.forClass(OnboardingPaRequest.class);
        verify(onboardingControllerApi, times(1))
                ._v1OnboardingPaCompletionPost(onboardingRequestCaptor.capture());
        OnboardingPaRequest actual = onboardingRequestCaptor.getValue();
        assertEquals(actual.getInstitution().getTaxCode(), institutionUpdate.getTaxCode());
        verifyNoMoreInteractions(onboardingControllerApi);
    }
    @Test
    void onboarding_institutionPsp() {
        // given
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
        // when
        onboardingMsConnector.onboarding(onboardingData);
        // then
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
        // given
        OnboardingData onboardingData = new OnboardingData();
        onboardingData.setTaxCode("taxCode");
        InstitutionUpdate institutionUpdate = new InstitutionUpdate();
        institutionUpdate.setTaxCode("taxCode");
        onboardingData.setUsers(List.of(mockInstance(new User())));
        onboardingData.setInstitutionUpdate(institutionUpdate);
        // when
        onboardingMsConnector.onboardingImportPA(onboardingData);
        // then
        ArgumentCaptor<OnboardingImportRequest> onboardingRequestCaptor = ArgumentCaptor.forClass(OnboardingImportRequest.class);
        verify(onboardingControllerApi, times(1))
                ._v1OnboardingPaImportPost(onboardingRequestCaptor.capture());
        OnboardingImportRequest actual = onboardingRequestCaptor.getValue();
        assertEquals(actual.getInstitution().getTaxCode(), institutionUpdate.getTaxCode());
        verifyNoMoreInteractions(onboardingControllerApi);
    }
}
