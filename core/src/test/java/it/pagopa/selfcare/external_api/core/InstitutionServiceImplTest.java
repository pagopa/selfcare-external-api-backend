package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.external_api.api.MsCoreConnector;
import it.pagopa.selfcare.external_api.api.ProductsConnector;
import it.pagopa.selfcare.external_api.api.UserMsConnector;
import it.pagopa.selfcare.external_api.api.UserRegistryConnector;
import it.pagopa.selfcare.external_api.core.config.CoreTestConfig;
import it.pagopa.selfcare.external_api.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.external_api.model.institutions.GeographicTaxonomy;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.institutions.SearchMode;
import it.pagopa.selfcare.external_api.model.pnpg.CreatePnPgInstitution;
import it.pagopa.selfcare.external_api.model.product.Product;
import it.pagopa.selfcare.external_api.model.user.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.external_api.core.InstitutionServiceImpl.REQUIRED_INSTITUTION_MESSAGE;
import static it.pagopa.selfcare.external_api.model.user.RelationshipState.ACTIVE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        InstitutionServiceImpl.class,
        CoreTestConfig.class
})
@TestPropertySource(properties = {
        "ALLOWED_SERVICE_TYPES=external-interceptor,onboarding-interceptor"
})
class InstitutionServiceImplTest {
    @Autowired
    private InstitutionServiceImpl institutionService;

    @MockBean
    private ProductsConnector productsConnectorMock;

    @MockBean
    private UserRegistryConnector userRegistryConnectorMock;

    @MockBean
    private MsCoreConnector msCoreConnectorMock;

    @MockBean
    private UserMsConnector userMsConnector;

    @BeforeEach
    void beforeEach() {
        TestSecurityContextHolder.clearContext();
    }


    @Test
    void getInstitutionUserProducts_V2() {
        //given
        final String institutionId = "institutionId";
        final String userId = UUID.randomUUID().toString();
        final SelfCareUser selfCareUser = SelfCareUser.builder(userId)
                .email("test@example.com")
                .name("name")
                .surname("surname")
                .build();
        TestSecurityContextHolder.setAuthentication(new TestingAuthenticationToken(selfCareUser, null));
        final Product product1 = mockInstance(new Product(), 1);
        final Product product2 = mockInstance(new Product(), 2);
        final Product product3 = mockInstance(new Product(), 3);
        final Product product4 = mockInstance(new Product(), 4);
        product1.setId("prod-io");
        product2.setId("prod-interop");
        product3.setId("id3");
        product4.setId("id4");
        final List<String> productIds = List.of("prod-io", "prod-interop");
        final List<Product> products = List.of(product1, product2, product3, product4);
        when(msCoreConnectorMock.getInstitutionUserProductsV2(any(), any()))
                .thenReturn(productIds);
        when(productsConnectorMock.getProducts())
                .thenReturn(products);
        //when
        List<Product> result = institutionService.getInstitutionUserProductsV2(institutionId);
        //then
        assertEquals(2, result.size());
        verify(msCoreConnectorMock, times(1))
                .getInstitutionUserProductsV2(institutionId, userId);
        verify(productsConnectorMock, times(1))
                .getProducts();
        verifyNoMoreInteractions(msCoreConnectorMock, productsConnectorMock);
    }


    @Test
    void getInstitutionProductUsersV2_nullInstitutionId() {
        // given
        final String institutionId = null;
        final String productId = "productId";
        final String userId = null;
        final Optional<Set<String>> productRole = Optional.empty();
        // when
        Executable executable = () -> institutionService.getInstitutionProductUsersV2(institutionId, productId, userId, productRole, null);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals(REQUIRED_INSTITUTION_MESSAGE, e.getMessage());
        verifyNoInteractions(productsConnectorMock, msCoreConnectorMock);
    }
    @Test
    void getInstitutionProductUsersV2_nullProductId() {
        // given
        final String institutionId = "institutionId";
        final String productId = null;
        final String userId = "userId";
        final Optional<Set<String>> productRole = Optional.empty();
        // when
        Executable executable = () -> institutionService.getInstitutionProductUsersV2(institutionId, productId, userId, productRole, null);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("A Product id is required", e.getMessage());
        verifyNoInteractions(productsConnectorMock, msCoreConnectorMock);
    }






    @Test
    void getInstitutionProductUsersV2() {
        // given
        final String institutionId = "institutionId";
        final String productId = "productId";

        final Optional<Set<String>> productRole = Optional.empty();

        OnboardedProductResponse onboardedProductResponse = new OnboardedProductResponse();
        onboardedProductResponse.setProductId(productId);
        onboardedProductResponse.setStatus(ACTIVE.name());
        onboardedProductResponse.setRole(PartyRole.DELEGATE.name());
        onboardedProductResponse.setProductRole(PartyRole.DELEGATE.getSelfCareAuthority().name());

        final UserInstitution userInstitution = mockInstance(new UserInstitution());
        final String userId = UUID.randomUUID().toString();
        userInstitution.setInstitutionId(institutionId);
        userInstitution.setUserId(userId);
        userInstitution.setProducts(List.of(onboardedProductResponse));
        final User user = mockInstance(new User());
        user.setId(userId);
        WorkContact contact = mockInstance(new WorkContact());
        Map<String, WorkContact> workContact = new HashMap<>();
        workContact.put(institutionId, contact);
        user.setWorkContacts(workContact);
        when(userMsConnector.getUsersInstitutions(userId, institutionId, null, null, null, null, null, null))
                .thenReturn(Collections.singletonList(userInstitution));
        when(userRegistryConnectorMock.getUserByInternalId(anyString(), any()))
                .thenReturn(user);
        // when
        Collection<UserInfo> userInfos = institutionService.getInstitutionProductUsersV2(institutionId, productId, userId, productRole, null);
        // then
        Assertions.assertNotNull(userInfos);
        userInfos.forEach(userInfo1 -> {
            TestUtils.checkNotNullFields(userInfo1, "products");
            TestUtils.checkNotNullFields(userInfo1.getUser());
        });

        verify(userMsConnector, times(1))
                .getUsersInstitutions(userId, institutionId, null, null, null, null, null, null);

        ArgumentCaptor<EnumSet<User.Fields>> filedsCaptor = ArgumentCaptor.forClass(EnumSet.class);
        verify(userRegistryConnectorMock, times(1))
                .getUserByInternalId(eq(userId), filedsCaptor.capture());
        EnumSet<User.Fields> capturedFields = filedsCaptor.getValue();
        assertTrue(capturedFields.contains(User.Fields.name));
        assertTrue(capturedFields.contains(User.Fields.familyName));
        assertTrue(capturedFields.contains(User.Fields.workContacts));
        assertFalse(capturedFields.contains(User.Fields.fiscalCode));
        verifyNoMoreInteractions(userMsConnector, userRegistryConnectorMock);
        verifyNoInteractions(productsConnectorMock);
    }




    @Test
    void getInstitutionProductUsers_userIdFilterIsNull(){
        // given
        final String institutionId = "institutionId";
        final String productId = "productId";
        final String xSelfCareUid = "unregistered-interceptor";
        final Optional<Set<String>> productRole = Optional.empty();

        final String userId = UUID.randomUUID().toString();
        final User user = mockInstance(new User());
        user.setId(userId);
        WorkContact contact = mockInstance(new WorkContact());
        Map<String, WorkContact> workContact = new HashMap<>();
        workContact.put(institutionId, contact);

        OnboardedProductResponse onboardedProductResponse = new OnboardedProductResponse();
        onboardedProductResponse.setProductId(productId);
        onboardedProductResponse.setStatus(ACTIVE.name());
        onboardedProductResponse.setRole(PartyRole.DELEGATE.name());
        onboardedProductResponse.setProductRole(PartyRole.DELEGATE.getSelfCareAuthority().name());

        final UserInstitution userInstitution = mockInstance(new UserInstitution());
        userInstitution.setInstitutionId(institutionId);
        userInstitution.setUserId(userId);
        userInstitution.setProducts(List.of(onboardedProductResponse));

        user.setWorkContacts(workContact);
        when(userMsConnector.getUsersInstitutions(null, institutionId, null, null, null, null, null, null))
                .thenReturn(Collections.singletonList(userInstitution));
        when(userRegistryConnectorMock.getUserByInternalId(anyString(), any()))
                .thenReturn(user);
        // when
        Collection<UserInfo> userInfos = institutionService.getInstitutionProductUsersV2(institutionId, productId, null, productRole, xSelfCareUid);
        // then
        Assertions.assertNotNull(userInfos);
        userInfos.forEach(userInfo1 -> {
            TestUtils.checkNotNullFields(userInfo1, "products");
            TestUtils.checkNotNullFields(userInfo1.getUser());
        });
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(userMsConnector, times(1))
                .getUsersInstitutions(null, institutionId, null, null, null, null, null, null);

        ArgumentCaptor<EnumSet<User.Fields>> filedsCaptor = ArgumentCaptor.forClass(EnumSet.class);
        verify(userRegistryConnectorMock, times(1))
                .getUserByInternalId(eq(userId), filedsCaptor.capture());
        EnumSet<User.Fields> capturedFields = filedsCaptor.getValue();
        assertTrue(capturedFields.contains(User.Fields.name));
        assertTrue(capturedFields.contains(User.Fields.familyName));
        assertTrue(capturedFields.contains(User.Fields.workContacts));
        assertFalse(capturedFields.contains(User.Fields.fiscalCode));
        verifyNoMoreInteractions(userMsConnector, userRegistryConnectorMock);
        verifyNoInteractions(productsConnectorMock);
    }


    @Test
    void getGeographicTaxonomyList() {
        // given
        final String institutionId = "institutionId";
        final Institution institutionMock = mockInstance(new Institution());
        institutionMock.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        when(msCoreConnectorMock.getGeographicTaxonomyList(anyString()))
                .thenReturn(institutionMock.getGeographicTaxonomies());
        // when
        List<GeographicTaxonomy> result = institutionService.getGeographicTaxonomyList(institutionId);
        // then
        assertNotNull(result);
        assertEquals(institutionMock.getGeographicTaxonomies().get(0).getCode(), result.get(0).getCode());
        assertEquals(institutionMock.getGeographicTaxonomies().get(0).getDesc(), result.get(0).getDesc());
        verify(msCoreConnectorMock, times(1))
                .getGeographicTaxonomyList(institutionId);
        verifyNoMoreInteractions(msCoreConnectorMock);
    }

    @Test
    void getGeographicTaxonomyList_hasNullInstitutionId() {
        // given
        final String institutionId = null;
        // when
        Executable executable = () -> institutionService.getGeographicTaxonomyList(institutionId);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_INSTITUTION_MESSAGE, e.getMessage());
        verifyNoInteractions(msCoreConnectorMock);
    }

    @Test
    void getInstitutionsByGeoTaxonomies() {
        //given
        final Set<String> geoTaxIds = Set.of("geoTax1", "geoTax2");
        final SearchMode searchMode = SearchMode.any;
        when(msCoreConnectorMock.getInstitutionsByGeoTaxonomies(anyString(), any()))
                .thenReturn(List.of(mockInstance(new Institution())));
        //when
        Collection<Institution> results = institutionService.getInstitutionsByGeoTaxonomies(geoTaxIds, searchMode);
        //then
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        ArgumentCaptor<String> geoTaxIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(msCoreConnectorMock, times(1))
                .getInstitutionsByGeoTaxonomies(geoTaxIdCaptor.capture(), eq(searchMode));
        assertEquals(String.join(",", geoTaxIds), geoTaxIdCaptor.getValue());
        verifyNoInteractions(userRegistryConnectorMock, productsConnectorMock);
        verifyNoMoreInteractions(msCoreConnectorMock);
    }

    @Test
    void getInstitutionsByGeoTaxonomies_nullGeoTaxIds() {
        //given
        final Set<String> geoTax = null;
        final SearchMode searchMode = SearchMode.any;
        //when
        Executable executable = () -> institutionService.getInstitutionsByGeoTaxonomies(geoTax, searchMode);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("GeoTaxonomy ids are required in order to retrieve the institutions", e.getMessage());
        verifyNoInteractions(userRegistryConnectorMock, productsConnectorMock, msCoreConnectorMock);
    }
    @Test
    void addInstitution_exists() {
        //given
        CreatePnPgInstitution createPnPgInstitution = mockInstance(new CreatePnPgInstitution());
        Institution institution = mockInstance(new Institution());
        when(msCoreConnectorMock.getInstitutionByExternalId(anyString()))
                .thenReturn(institution);
        //when
        String internalInstitutionId = institutionService.addInstitution(createPnPgInstitution);
        //then
        assertEquals(institution.getId(), internalInstitutionId);
        verify(msCoreConnectorMock, times(1))
                .getInstitutionByExternalId(createPnPgInstitution.getExternalId());
        verifyNoMoreInteractions(msCoreConnectorMock);
    }

    @Test
    void addInstitution_notExists() {
        //given
        final CreatePnPgInstitution createPnPgInstitution = mockInstance(new CreatePnPgInstitution());
        final String institutionId = "institutionId";
        doThrow(ResourceNotFoundException.class).
                when(msCoreConnectorMock)
                .getInstitutionByExternalId(anyString());
        when(msCoreConnectorMock.createPnPgInstitution(any()))
                .thenReturn(institutionId);
        //when
        String internalInstitutionId = institutionService.addInstitution(createPnPgInstitution);
        //then
        assertEquals(institutionId, internalInstitutionId);
        verify(msCoreConnectorMock, times(1))
                .getInstitutionByExternalId(createPnPgInstitution.getExternalId());
        verify(msCoreConnectorMock, times(1))
                .createPnPgInstitution(createPnPgInstitution);
        verifyNoMoreInteractions(msCoreConnectorMock);
    }
}
