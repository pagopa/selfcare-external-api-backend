package it.pagopa.selfcare.external_api.connector.rest;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.external_api.connector.rest.client.PartyProcessRestClient;
import it.pagopa.selfcare.external_api.connector.rest.model.OnBoardingInfo;
import it.pagopa.selfcare.external_api.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingResponseData;
import it.pagopa.selfcare.external_api.model.onboarding.ProductInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.external_api.model.onboarding.RelationshipState.ACTIVE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartyConnectorImplTest {

    @InjectMocks
    private PartyConnectorImpl partyConnector;

    @Mock
    private PartyProcessRestClient restClientMock;

    @Test
    void getOnboardedInstitutions_merge() {
        //given
        String productId = "productId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        ProductInfo product = mockInstance(new ProductInfo());
        product.setId(productId);
        OnboardingResponseData onboardingData1 = mockInstance(new OnboardingResponseData(), 1, "setState", "setRole");
        onboardingData1.setState(ACTIVE);
        onboardingData1.setRole(PartyRole.OPERATOR);
        onboardingData1.setProductInfo(product);
        OnboardingResponseData onboardingData2 = mockInstance(new OnboardingResponseData(), 2, "setState", "setId", "setRole");
        onboardingData2.setState(ACTIVE);
        onboardingData2.setId(onboardingData1.getId());
        onboardingData2.setRole(PartyRole.MANAGER);
        onboardingData2.setProductInfo(product);
        OnboardingResponseData onboardingData4 = mockInstance(new OnboardingResponseData(), 4, "setState", "setId", "setRole");
        onboardingData4.setState(ACTIVE);
        onboardingData4.setId(onboardingData1.getId());
        onboardingData4.setRole(PartyRole.SUB_DELEGATE);
        onboardingData4.setProductInfo(product);
        OnboardingResponseData onboardingData3 = mockInstance(new OnboardingResponseData(), 3, "setState", "setRole");
        onboardingData3.setState(ACTIVE);
        onboardingData3.setRole(PartyRole.OPERATOR);
        onboardingData3.setProductInfo(product);
        onBoardingInfo.setInstitutions(List.of(onboardingData1, onboardingData2, onboardingData3, onboardingData3, onboardingData4));
        when(restClientMock.getOnBoardingInfo(any(), any()))
                .thenReturn(onBoardingInfo);
        // when
        Collection<InstitutionInfo> institutions = partyConnector.getOnBoardedInstitutions(productId);
        // then
        assertNotNull(institutions);
        assertEquals(2, institutions.size());
        Map<PartyRole, List<InstitutionInfo>> map = institutions.stream()
                .collect(Collectors.groupingBy(InstitutionInfo::getUserRole));
        List<InstitutionInfo> institutionInfos = map.get(PartyRole.MANAGER);
        assertNotNull(institutionInfos);
        assertEquals(1, institutionInfos.size());
        assertEquals(onboardingData2.getId(), institutionInfos.get(0).getId());
        assertEquals(onboardingData2.getDescription(), institutionInfos.get(0).getDescription());
        assertEquals(onboardingData2.getExternalId(), institutionInfos.get(0).getExternalId());
        assertEquals(onboardingData2.getState().toString(), institutionInfos.get(0).getStatus());
        assertEquals(onboardingData2.getRole(), institutionInfos.get(0).getUserRole());
        institutionInfos = map.get(PartyRole.OPERATOR);
        assertNotNull(institutionInfos);
        assertEquals(1, institutionInfos.size());
        assertEquals(onboardingData3.getId(), institutionInfos.get(0).getId());
        assertEquals(onboardingData3.getDescription(), institutionInfos.get(0).getDescription());
        assertEquals(onboardingData3.getExternalId(), institutionInfos.get(0).getExternalId());
        assertEquals(onboardingData3.getState().toString(), institutionInfos.get(0).getStatus());
        assertEquals(onboardingData3.getRole(), institutionInfos.get(0).getUserRole());
        verify(restClientMock, times(1))
                .getOnBoardingInfo(isNull(), eq(EnumSet.of(ACTIVE)));
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getOnboardedInstitutions_productFilter(){
        //given
        String productId = "productId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        ProductInfo product1 = mockInstance(new ProductInfo(), 1);
        ProductInfo product2 = mockInstance(new ProductInfo(), 2);
        ProductInfo product3 = mockInstance(new ProductInfo(), 3);
        product1.setId(productId);
        product2.setId(productId);
        OnboardingResponseData onboardingData1 = mockInstance(new OnboardingResponseData(), 1, "setState", "setRole");
        onboardingData1.setState(ACTIVE);
        onboardingData1.setRole(PartyRole.OPERATOR);
        onboardingData1.setProductInfo(product1);
        OnboardingResponseData onboardingData2 = mockInstance(new OnboardingResponseData(), 2, "setState", "setId", "setRole");
        onboardingData2.setState(ACTIVE);
        onboardingData2.setRole(PartyRole.MANAGER);
        onboardingData2.setProductInfo(product1);
        OnboardingResponseData onboardingData4 = mockInstance(new OnboardingResponseData(), 4, "setState", "setId", "setRole");
        onboardingData4.setState(ACTIVE);
        onboardingData4.setRole(PartyRole.SUB_DELEGATE);
        onboardingData4.setProductInfo(product2);
        OnboardingResponseData onboardingData3 = mockInstance(new OnboardingResponseData(), 3, "setState", "setRole");
        onboardingData3.setState(ACTIVE);
        onboardingData3.setRole(PartyRole.OPERATOR);
        onboardingData3.setProductInfo(product3);
        onBoardingInfo.setInstitutions(List.of(onboardingData1, onboardingData2, onboardingData3, onboardingData3, onboardingData4));
        when(restClientMock.getOnBoardingInfo(any(), any()))
                .thenReturn(onBoardingInfo);
        // when
        Collection<InstitutionInfo> institutions = partyConnector.getOnBoardedInstitutions(productId);
        // then
        assertNotNull(institutions);
        assertEquals(2, institutions.size());
        verify(restClientMock, times(1))
                .getOnBoardingInfo(isNull(), eq(EnumSet.of(ACTIVE)));
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getOnboardedInstitutions_nullOnboardingInfo() {
        //given
        String productId = "productId";
        //when
        Collection<InstitutionInfo> institutionInfos = partyConnector.getOnBoardedInstitutions(productId);
        //then
        assertNotNull(institutionInfos);
        assertTrue(institutionInfos.isEmpty());
        verify(restClientMock, times(1))
                .getOnBoardingInfo(isNull(), Mockito.isNotNull());
        verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getOnboardedInstitutions_nullProductId(){
        //given
        String productId = null;
        //when
        Executable executable = () -> partyConnector.getOnBoardedInstitutions(productId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A productId is required", e.getMessage());
        verifyNoInteractions(restClientMock);
    }

    @Test
    void getOnboardedInstitutions_nullInstitutions() {
        //given
        String productId = "productId";
        OnBoardingInfo onboardingInfo = new OnBoardingInfo();
        when(restClientMock.getOnBoardingInfo(any(), any()))
                .thenReturn(onboardingInfo);
        //when
        Collection<InstitutionInfo> institutionInfos = partyConnector.getOnBoardedInstitutions(productId);
        //then
        assertNotNull(institutionInfos);
        assertTrue(institutionInfos.isEmpty());
        verify(restClientMock, times(1))
                .getOnBoardingInfo(isNull(), Mockito.isNotNull());
        verifyNoMoreInteractions(restClientMock);
    }


}