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
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.external_api.connector.rest.client.PartyManagementRestClient;
import it.pagopa.selfcare.external_api.connector.rest.client.PartyProcessRestClient;
import it.pagopa.selfcare.external_api.connector.rest.model.institution.Institutions;
import it.pagopa.selfcare.external_api.connector.rest.model.institution.OnBoardingInfo;
import it.pagopa.selfcare.external_api.connector.rest.model.institution.RelationshipsResponse;
import it.pagopa.selfcare.external_api.connector.rest.model.relationship.Relationship;
import it.pagopa.selfcare.external_api.connector.rest.model.relationship.Relationships;
import it.pagopa.selfcare.external_api.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.external_api.model.institutions.*;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingResponseData;
import it.pagopa.selfcare.external_api.model.product.PartyProduct;
import it.pagopa.selfcare.external_api.model.product.ProductInfo;
import it.pagopa.selfcare.external_api.model.user.UserInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ResourceUtils;

import javax.validation.ValidationException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.commons.base.security.PartyRole.MANAGER;
import static it.pagopa.selfcare.commons.base.security.SelfCareAuthority.ADMIN;
import static it.pagopa.selfcare.commons.base.security.SelfCareAuthority.LIMITED;
import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.external_api.connector.rest.PartyConnectorImpl.INSTITUTION_ID_IS_REQUIRED;
import static it.pagopa.selfcare.external_api.connector.rest.PartyConnectorImpl.USER_ID_IS_REQUIRED;
import static it.pagopa.selfcare.external_api.model.user.RelationshipState.ACTIVE;
import static it.pagopa.selfcare.external_api.model.user.RelationshipState.SUSPENDED;
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
    private PartyProcessRestClient partyProcessRestClientMock;

    @Mock
    private PartyManagementRestClient partyManagementRestClientMock;


    @Test
    void getOnboardedInstitutions_merge() throws IOException {
        //given
        String productId = "productId";
        File stubs = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/onboardingInfo.json");
        OnBoardingInfo onBoardingInfo = mapper.readValue(stubs, OnBoardingInfo.class);
        when(partyProcessRestClientMock.getOnBoardingInfo(any(), any()))
                .thenReturn(onBoardingInfo);
        // when
        Collection<InstitutionInfo> institutions = partyConnector.getOnBoardedInstitutions(productId);
        // then
        assertNotNull(institutions);
        assertEquals(2, institutions.size());
        Map<PartyRole, List<InstitutionInfo>> map = institutions.stream()
                .collect(Collectors.groupingBy(InstitutionInfo::getUserRole));
        List<InstitutionInfo> institutionInfos = map.get(MANAGER);
        assertNotNull(institutionInfos);
        assertEquals(1, institutionInfos.size());
        assertEquals(3, institutionInfos.get(0).getProductRoles().size());
        assertEquals(onBoardingInfo.getInstitutions().get(0).getId(), institutionInfos.get(0).getId());
        institutionInfos = map.get(PartyRole.SUB_DELEGATE);
        assertNotNull(institutionInfos);
        assertEquals(1, institutionInfos.size());
        assertEquals(2, institutionInfos.get(0).getProductRoles().size());
        verify(partyProcessRestClientMock, times(1))
                .getOnBoardingInfo(isNull(), eq(EnumSet.of(ACTIVE)));
        verifyNoMoreInteractions(partyProcessRestClientMock);
        verifyNoInteractions(partyManagementRestClientMock);
    }

    @Test
    void getOnboardedInstitutions_productFilter() {
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
        onboardingData2.setRole(MANAGER);
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
        when(partyProcessRestClientMock.getOnBoardingInfo(any(), any()))
                .thenReturn(onBoardingInfo);
        // when
        Collection<InstitutionInfo> institutions = partyConnector.getOnBoardedInstitutions(productId);
        // then
        assertNotNull(institutions);
        assertEquals(2, institutions.size());
        verify(partyProcessRestClientMock, times(1))
                .getOnBoardingInfo(isNull(), eq(EnumSet.of(ACTIVE)));
        verifyNoMoreInteractions(partyProcessRestClientMock);
        verifyNoInteractions(partyManagementRestClientMock);
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
        verify(partyProcessRestClientMock, times(1))
                .getOnBoardingInfo(isNull(), isNotNull());
        verifyNoMoreInteractions(partyProcessRestClientMock);
        verifyNoInteractions(partyManagementRestClientMock);
    }


    @Test
    void getOnboardedInstitutions_nullProductId() {
        //given
        String productId = null;
        //when
        Executable executable = () -> partyConnector.getOnBoardedInstitutions(productId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A productId is required", e.getMessage());
        verifyNoInteractions(partyProcessRestClientMock, partyManagementRestClientMock);
    }

    @Test
    void getOnboardedInstitutions_nullInstitutions() {
        //given
        String productId = "productId";
        OnBoardingInfo onboardingInfo = new OnBoardingInfo();
        when(partyProcessRestClientMock.getOnBoardingInfo(any(), any()))
                .thenReturn(onboardingInfo);
        //when
        Collection<InstitutionInfo> institutionInfos = partyConnector.getOnBoardedInstitutions(productId);
        //then
        assertNotNull(institutionInfos);
        assertTrue(institutionInfos.isEmpty());
        verify(partyProcessRestClientMock, times(1))
                .getOnBoardingInfo(isNull(), isNotNull());
        verifyNoMoreInteractions(partyProcessRestClientMock);
        verifyNoInteractions(partyManagementRestClientMock);
    }

    @Test
    void getInstitutionUserProducts_nullInstitutionId() {
        //given
        String institutionId = null;
        String userId = "userId";
        //when
        Executable executable = () -> partyConnector.getInstitutionUserProducts(institutionId, userId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(INSTITUTION_ID_IS_REQUIRED, e.getMessage());
        verifyNoInteractions(partyProcessRestClientMock);
        verifyNoInteractions(partyManagementRestClientMock);
    }

    @Test
    void getInstitutionUserProducts_nullUserId() {
        //given
        String institutionId = "institutionId";
        String userId = null;
        //when
        Executable executable = () -> partyConnector.getInstitutionUserProducts(institutionId, userId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(USER_ID_IS_REQUIRED, e.getMessage());
        verifyNoInteractions(partyProcessRestClientMock);
        verifyNoInteractions(partyManagementRestClientMock);
    }

    @Test
    void getInstitutionUserProducts_nullResponse() {
        //given
        String institutionId = "institutionId";
        String userId = "userId";
        //when
        List<PartyProduct> products = partyConnector.getInstitutionUserProducts(institutionId, userId);
        //then
        assertNotNull(products);
        assertTrue(products.isEmpty());
        verify(partyProcessRestClientMock, times(1))
                .getUserInstitutionRelationships(eq(institutionId),
                        eq(EnumSet.allOf(PartyRole.class)),
                        eq(EnumSet.of(ACTIVE)),
                        isNull(),
                        isNull(),
                        eq(userId));
        verifyNoMoreInteractions(partyProcessRestClientMock);
        verifyNoInteractions(partyManagementRestClientMock);
    }

    @Test
    void getInstitutionUserProducts() throws IOException {
        //given
        String institutionId = "institutionId";
        String userId = "userId";

        File stubs = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/relationshipInfo_to_product.json");
        RelationshipsResponse response = mapper.readValue(stubs, RelationshipsResponse.class);
        when(partyProcessRestClientMock.getUserInstitutionRelationships(any(), any(), any(), any(), any(), any()))
                .thenReturn(response);
        //when
        List<PartyProduct> products = partyConnector.getInstitutionUserProducts(institutionId, userId);
        //then
        assertNotNull(products);
        assertEquals(3, products.size());
        assertEquals(PartyRole.DELEGATE, products.get(0).getRole());
        assertEquals(PartyRole.OPERATOR, products.get(1).getRole());
        assertEquals(PartyRole.OPERATOR, products.get(2).getRole());
        verify(partyProcessRestClientMock, times(1))
                .getUserInstitutionRelationships(eq(institutionId),
                        eq(EnumSet.allOf(PartyRole.class)),
                        eq(EnumSet.of(ACTIVE)),
                        isNull(),
                        isNull(),
                        eq(userId));
        verifyNoMoreInteractions(partyProcessRestClientMock);
        verifyNoInteractions(partyManagementRestClientMock);
    }


    @Test
    void getUsers() {
        // given
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setAllowedState(Optional.of(EnumSet.of(ACTIVE, SUSPENDED)));
        userInfoFilter.setRole(Optional.of(MANAGER.toString()));

        Relationship relationship1 = TestUtils.mockInstance(new Relationship(), "setFrom");
        String id = "id";
        relationship1.setFrom(id);
        Relationship relationship2 = TestUtils.mockInstance(new Relationship(), "setFrom");
        relationship2.setFrom(id);
        Relationships relationships = new Relationships();
        relationships.setItems(List.of(relationship1, relationship2));
        when(partyManagementRestClientMock.getRelationships(any(), any(), any(), any(), any(), any()))
                .thenReturn(relationships);
        // when
        Collection<UserInfo> userInfos = partyConnector.getUsers(userInfoFilter);
        // then
        assertNotNull(userInfos);
        assertEquals(1, userInfos.size());
        UserInfo userInfo = userInfos.iterator().next();
        String prodId = null;
        Map<String, it.pagopa.selfcare.external_api.model.user.ProductInfo> productInfoMap = userInfo.getProducts();
        for (String key :
                productInfoMap.keySet()) {
            prodId = key;
        }
        it.pagopa.selfcare.external_api.model.user.ProductInfo product = productInfoMap.get(prodId);
        assertEquals(id, userInfo.getId());
        assertNotNull(product.getRoleInfos());
        assertNotNull(product.getId());
        assertNull(product.getTitle());
        assertNull(userInfo.getUser());
        assertNotNull(userInfo.getStatus());
        assertNotNull(userInfo.getRole());
        assertEquals(1, userInfo.getProducts().size());
        assertNotNull(productInfoMap.keySet());
        verify(partyManagementRestClientMock, times(1))
                .getRelationships(isNull(), isNull(), notNull(), notNull(), isNull(), any());
        verifyNoMoreInteractions(partyManagementRestClientMock);
        verifyNoInteractions(partyProcessRestClientMock);
    }


    @Test
    void getUsers_nullRelationships() {
        //given
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        when(partyManagementRestClientMock.getRelationships(any(), any(), any(), any(), any(), any()))
                .thenReturn(null);
        //when
        Collection<UserInfo> userInfos = partyConnector.getUsers(userInfoFilter);
        //then
        assertTrue(userInfos.isEmpty());
        verify(partyManagementRestClientMock, times(1))
                .getRelationships(any(), any(), any(), any(), any(), any());
        verifyNoMoreInteractions(partyManagementRestClientMock);
        verifyNoInteractions(partyProcessRestClientMock);
    }


    @Test
    void getUsers_nullRelationshipsItems() {
        //given
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        when(partyManagementRestClientMock.getRelationships(any(), any(), any(), any(), any(), any()))
                .thenReturn(new Relationships());
        //when
        Collection<UserInfo> userInfos = partyConnector.getUsers(userInfoFilter);
        //then
        assertTrue(userInfos.isEmpty());
        verify(partyManagementRestClientMock, times(1))
                .getRelationships(any(), any(), any(), any(), any(), any());
        verifyNoMoreInteractions(partyManagementRestClientMock);
        verifyNoInteractions(partyProcessRestClientMock);
    }


    @Test
    void getUsers_mergeRoleInfos() throws IOException {
        //given
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        File stub = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/getUsers_multi-role.json");
        Relationships relationships = mapper.readValue(stub, Relationships.class);
        when(partyManagementRestClientMock.getRelationships(any(), any(), any(), any(), any(), any()))
                .thenReturn(relationships);
        //when
        Collection<UserInfo> userInfos = partyConnector.getUsers(userInfoFilter);
        //then
        assertEquals(1, userInfos.size());
        UserInfo userInfo = userInfos.iterator().next();
        Map<String, it.pagopa.selfcare.external_api.model.user.ProductInfo> productInfoMap = userInfo.getProducts();
        assertEquals(2, productInfoMap.values().size());
        assertEquals(2, productInfoMap.get("prod-io").getRoleInfos().size());
        assertEquals(1, productInfoMap.get("prod-pn").getRoleInfos().size());
        verify(partyManagementRestClientMock, times(1))
                .getRelationships(any(), any(), any(), any(), any(), any());
        verifyNoMoreInteractions(partyManagementRestClientMock);
        verifyNoInteractions(partyProcessRestClientMock);
    }


    @Test
    void getUsers_higherRoleForActiveUsers() throws IOException {
        //given
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        File stub = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/getUsers_higher-role-active.json");
        Relationships relationships = mapper.readValue(stub, Relationships.class);
        when(partyManagementRestClientMock.getRelationships(any(), any(), any(), any(), any(), any()))
                .thenReturn(relationships);
        //when
        Collection<UserInfo> userInfos = partyConnector.getUsers(userInfoFilter);
        //Then
        assertEquals(1, userInfos.size());
        UserInfo userInfo = userInfos.iterator().next();
        assertNull(userInfo.getUser());
        assertEquals(ADMIN, userInfo.getRole());
        assertEquals("ACTIVE", userInfo.getStatus());
        assertEquals(2, userInfo.getProducts().size());
        verify(partyManagementRestClientMock, times(1))
                .getRelationships(any(), any(), any(), any(), any(), any());
        verifyNoMoreInteractions(partyManagementRestClientMock);
        verifyNoInteractions(partyProcessRestClientMock);
    }


    @Test
    void getUser_getProductFromMerge() throws IOException {
        //given
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        File stub = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/getUsers_merge.json");
        Relationships relationships = mapper.readValue(stub, Relationships.class);
        when(partyManagementRestClientMock.getRelationships(any(), any(), any(), any(), any(), any()))
                .thenReturn(relationships);
        //when
        Collection<UserInfo> userInfos = partyConnector.getUsers(userInfoFilter);
        //then
        assertEquals(1, userInfos.size());
        UserInfo userInfo = userInfos.iterator().next();
        assertEquals(relationships.getItems().size(), userInfo.getProducts().size());
        assertNull(userInfo.getUser());
        assertEquals(ADMIN, userInfo.getRole());
        assertEquals("PENDING", userInfo.getStatus());
        verify(partyManagementRestClientMock, times(1))
                .getRelationships(any(), any(), any(), any(), any(), any());
        verifyNoMoreInteractions(partyManagementRestClientMock);
        verifyNoInteractions(partyProcessRestClientMock);
    }


    @Test
    void getUsers_higherRoleForPendingUsers() throws IOException {
        //given
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        File stub = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/getUsers_higher-role-pending.json");
        Relationships relationships = mapper.readValue(stub, Relationships.class);
        when(partyManagementRestClientMock.getRelationships(any(), any(), any(), any(), any(), any()))
                .thenReturn(relationships);
        //when
        Collection<UserInfo> userInfos = partyConnector.getUsers(userInfoFilter);
        UserInfo userInfo = userInfos.iterator().next();
        //Then
        assertNull(userInfo.getUser());
        assertEquals(ADMIN, userInfo.getRole());
        assertEquals("PENDING", userInfo.getStatus());
        assertEquals(1, userInfos.size());
        verify(partyManagementRestClientMock, times(1))
                .getRelationships(any(), any(), any(), any(), any(), any());
        verifyNoMoreInteractions(partyManagementRestClientMock);
        verifyNoInteractions(partyProcessRestClientMock);
    }


    @Test
    void getUsers_activeRoleUserDifferentStatus() throws IOException {
        //given
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        File stub = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/getUsers_active-role-different-status.json");
        Relationships relationships = mapper.readValue(stub, Relationships.class);
        when(partyManagementRestClientMock.getRelationships(any(), any(), any(), any(), any(), any()))
                .thenReturn(relationships);
        //when
        Collection<UserInfo> userInfos = partyConnector.getUsers(userInfoFilter);
        UserInfo userInfo = userInfos.iterator().next();
        //Then
        assertNull(userInfo.getUser());
        assertEquals(LIMITED, userInfo.getRole());
        assertEquals("ACTIVE", userInfo.getStatus());
        assertEquals(1, userInfos.size());
        verify(partyManagementRestClientMock, times(1))
                .getRelationships(any(), any(), any(), any(), any(), any());
        verifyNoMoreInteractions(partyManagementRestClientMock);
        verifyNoInteractions(partyProcessRestClientMock);
    }


    @Test
    void getUsers_activeRoleUserDifferentStatus_2() throws IOException {
        //given
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        File stub = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/getUsers_active-role-different-status-2.json");
        Relationships relationships = mapper.readValue(stub, Relationships.class);
        when(partyManagementRestClientMock.getRelationships(any(), any(), any(), any(), any(), any()))
                .thenReturn(relationships);
        //when
        Collection<UserInfo> userInfos = partyConnector.getUsers(userInfoFilter);
        UserInfo userInfo = userInfos.iterator().next();
        //Then
        assertNull(userInfo.getUser());
        assertEquals(ADMIN, userInfo.getRole());
        assertEquals("ACTIVE", userInfo.getStatus());
        assertEquals(1, userInfos.size());
        verify(partyManagementRestClientMock, times(1))
                .getRelationships(any(), any(), any(), any(), any(), any());
        verifyNoMoreInteractions(partyManagementRestClientMock);
        verifyNoInteractions(partyProcessRestClientMock);
    }

    @Test
    void getGeographicTaxonomyList_nullInstitutionId() {
        // given
        String institutionId = null;
        // when
        Executable executable = () -> partyConnector.getGeographicTaxonomyList(institutionId);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(INSTITUTION_ID_IS_REQUIRED, e.getMessage());
        verifyNoInteractions(partyProcessRestClientMock, partyManagementRestClientMock);
    }

    @Test
    void getGeographicTaxonomyList_noGeographicTaxonomies() {
        // given
        String institutionId = "institutionId";
        Institution institutionMock = mockInstance(new Institution());
        when(partyProcessRestClientMock.getInstitution(any()))
                .thenReturn(institutionMock);
        // when
        Executable executable = () -> partyConnector.getGeographicTaxonomyList(institutionId);
        // then
        ValidationException e = assertThrows(ValidationException.class, executable);
        assertEquals(String.format("The institution %s does not have geographic taxonomies.", institutionId), e.getMessage());
        verify(partyProcessRestClientMock, times(1))
                .getInstitution(institutionId);
        verifyNoMoreInteractions(partyProcessRestClientMock);
        verifyNoInteractions(partyManagementRestClientMock);
    }

    @Test
    void getGeographicTaxonomyList() {
        // given
        String institutionId = "institutionId";
        Institution institutionMock = mockInstance(new Institution());
        Attribute attribute = mockInstance(new Attribute());
        institutionMock.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        institutionMock.setAttributes(List.of(attribute));
        when(partyProcessRestClientMock.getInstitution(any()))
                .thenReturn(institutionMock);
        // when
        List<GeographicTaxonomy> geographicTaxonomies = partyConnector.getGeographicTaxonomyList(institutionId);
        // then
        assertSame(institutionMock.getGeographicTaxonomies(), geographicTaxonomies);
        assertNotNull(geographicTaxonomies);
        verify(partyProcessRestClientMock, times(1))
                .getInstitution(institutionId);
        verifyNoMoreInteractions(partyProcessRestClientMock);
        verifyNoInteractions(partyManagementRestClientMock);
    }

    @Test
    void getInstitutionsByGeoTaxonomies() throws IOException {
        //given
        String geoTaxIds = "taxId";
        SearchMode searchMode = SearchMode.any;
        File stub = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/institutions.json");
        Institutions institutions = mapper.readValue(stub, Institutions.class);

        when(partyManagementRestClientMock.getInstitutionsByGeoTaxonomies(anyString(), any()))
                .thenReturn(institutions);
        //when
        Collection<Institution> results = partyConnector.getInstitutionsByGeoTaxonomies(geoTaxIds, searchMode);
        //then
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(2, results.size());
        verify(partyManagementRestClientMock, times(1))
                .getInstitutionsByGeoTaxonomies(geoTaxIds, searchMode);
        verifyNoInteractions(partyProcessRestClientMock);
        verifyNoMoreInteractions(partyManagementRestClientMock);
    }

    @Test
    void getInstitutionsByGeoTaxonomies_nullResponse() {
        //given
        String geoTax = "geoTaxIds";
        SearchMode searchMode = SearchMode.any;
        when(partyManagementRestClientMock.getInstitutionsByGeoTaxonomies(any(), any()))
                .thenReturn(new Institutions());
        //when
        Executable executable = () -> partyConnector.getInstitutionsByGeoTaxonomies(geoTax, searchMode);
        //then
        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, executable);
        assertEquals(String.format("No institutions where found for given taxIds = %s", geoTax), e.getMessage());
        verify(partyManagementRestClientMock, times(1))
                .getInstitutionsByGeoTaxonomies(geoTax, searchMode);
        verifyNoMoreInteractions(partyManagementRestClientMock);
        verifyNoInteractions(partyProcessRestClientMock);
    }
}
