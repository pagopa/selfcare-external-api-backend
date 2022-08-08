package it.pagopa.selfcare.external_api.connector.rest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.external_api.connector.rest.client.PartyProcessRestClient;
import it.pagopa.selfcare.external_api.connector.rest.model.institution.OnBoardingInfo;
import it.pagopa.selfcare.external_api.connector.rest.model.institution.RelationshipsResponse;
import it.pagopa.selfcare.external_api.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingResponseData;
import it.pagopa.selfcare.external_api.model.product.PartyProduct;
import it.pagopa.selfcare.external_api.model.product.ProductInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.external_api.connector.rest.PartyConnectorImpl.INSTITUTION_ID_IS_REQUIRED;
import static it.pagopa.selfcare.external_api.connector.rest.PartyConnectorImpl.USER_ID_IS_REQUIRED;
import static it.pagopa.selfcare.external_api.model.user.RelationshipState.ACTIVE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartyConnectorImplTest {

    private final ObjectMapper mapper;

    public PartyConnectorImplTest() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new Jdk8Module());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setTimeZone(TimeZone.getDefault());
    }

    @InjectMocks
    private PartyConnectorImpl partyConnector;

    @Mock
    private PartyProcessRestClient restClientMock;

    @Test
    void getOnboardedInstitutions_merge() throws IOException {
        //given
        String productId = "productId";
        File stubs = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/onboardingInfo.json");
        OnBoardingInfo onBoardingInfo = mapper.readValue(stubs, OnBoardingInfo.class);
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
        assertEquals(3, institutionInfos.get(0).getProductRoles().size());
        assertEquals(onBoardingInfo.getInstitutions().get(0).getId(), institutionInfos.get(0).getId());
        institutionInfos = map.get(PartyRole.SUB_DELEGATE);
        assertNotNull(institutionInfos);
        assertEquals(1, institutionInfos.size());
        assertEquals(3, institutionInfos.get(0).getProductRoles().size());
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

    @Test
    void getInstitutionUserProducts_nullInstitutionId(){
        //given
        String institutionId = null;
        String userId = "userId";
        //when
        Executable executable = () -> partyConnector.getInstitutionUserProducts(institutionId, userId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(INSTITUTION_ID_IS_REQUIRED, e.getMessage());
        verifyNoInteractions(restClientMock);
    }

    @Test
    void getInstitutionUserProducts_nullUserId(){
        //given
        String institutionId = "institutionId";
        String userId = null;
        //when
        Executable executable = () -> partyConnector.getInstitutionUserProducts(institutionId, userId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(USER_ID_IS_REQUIRED, e.getMessage());
        verifyNoInteractions(restClientMock);
    }

    @Test
    void getInstitutionUserProducts_nullResponse(){
        //given
        String institutionId = "institutionId";
        String userId = "userId";
        //when
        List<PartyProduct> products = partyConnector.getInstitutionUserProducts(institutionId, userId);
        //then
        assertNotNull(products);
        assertTrue(products.isEmpty());
        verify(restClientMock, times(1))
                .getUserInstitutionRelationships(eq(institutionId),
                        eq(EnumSet.allOf(PartyRole.class)),
                        eq(EnumSet.of(ACTIVE)),
                        isNull(),
                        isNull(),
                        eq(userId));
    }
    
    @Test
    void getInstitutionUserProducts() throws IOException {
        //given
        String institutionId = "institutionId";
        String userId = "userId";

        File stubs = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/relationshipInfo_to_product.json");
        RelationshipsResponse response = mapper.readValue(stubs, RelationshipsResponse.class);
        when(restClientMock.getUserInstitutionRelationships(any(), any(), any(), any(), any(),any()))
                .thenReturn(response);
        //when
        List<PartyProduct> products = partyConnector.getInstitutionUserProducts(institutionId, userId);
        //then
        assertNotNull(products);
        assertEquals(3, products.size());
        assertEquals(PartyRole.DELEGATE, products.get(0).getRole());
        assertEquals(PartyRole.OPERATOR, products.get(1).getRole());
        assertEquals(PartyRole.OPERATOR, products.get(2).getRole());
        verify(restClientMock, times(1))
                .getUserInstitutionRelationships(eq(institutionId),
                        eq(EnumSet.allOf(PartyRole.class)),
                        eq(EnumSet.of(ACTIVE)),
                        isNull(),
                        isNull(),
                        eq(userId));
    }

}
