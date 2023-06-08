package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.external_api.api.MsCoreConnector;
import it.pagopa.selfcare.external_api.api.PartyConnector;
import it.pagopa.selfcare.external_api.api.ProductsConnector;
import it.pagopa.selfcare.external_api.api.UserRegistryConnector;
import it.pagopa.selfcare.external_api.core.config.CoreTestConfig;
import it.pagopa.selfcare.external_api.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.external_api.model.institutions.GeographicTaxonomy;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.external_api.model.institutions.SearchMode;
import it.pagopa.selfcare.external_api.model.pnpg.CreatePnPgInstitution;
import it.pagopa.selfcare.external_api.model.product.PartyProduct;
import it.pagopa.selfcare.external_api.model.product.Product;
import it.pagopa.selfcare.external_api.model.user.User;
import it.pagopa.selfcare.external_api.model.user.UserInfo;
import it.pagopa.selfcare.external_api.model.user.WorkContact;
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
    private PartyConnector partyConnectorMock;

    @MockBean
    private ProductsConnector productsConnectorMock;

    @MockBean
    private UserRegistryConnector userRegistryConnectorMock;

    @MockBean
    private MsCoreConnector msCoreConnectorMock;

    @BeforeEach
    void beforeEach() {
        TestSecurityContextHolder.clearContext();
    }


    @Test
    void getInstitutions() {
        //given
        String productId = "productIds";
        InstitutionInfo expectedInstitutionInfo = new InstitutionInfo();
        when(partyConnectorMock.getOnBoardedInstitutions(anyString()))
                .thenReturn(List.of(expectedInstitutionInfo));
        // when
        Collection<InstitutionInfo> institutions = institutionService.getInstitutions(productId);
        // then
        assertNotNull(institutions);
        assertEquals(1, institutions.size());
        assertSame(expectedInstitutionInfo, institutions.iterator().next());
        verify(partyConnectorMock, times(1))
                .getOnBoardedInstitutions(productId);
        verifyNoMoreInteractions(partyConnectorMock);
    }

    @Test
    void getInstitutions_emptyResult() {
        //given
        //when
        Collection<InstitutionInfo> institutions = institutionService.getInstitutions(null);
        // then
        assertNotNull(institutions);
        assertTrue(institutions.isEmpty());
        verify(partyConnectorMock, times(1))
                .getOnBoardedInstitutions(isNull());
        verifyNoMoreInteractions(partyConnectorMock);
    }

    @Test
    void getInstitutionUserProducts_nullAuth(){
        //given
        String institutionId = "institutionId";
        //when
        Executable executable = () -> institutionService.getInstitutionUserProducts(institutionId);
        //then
        IllegalStateException e = assertThrows(IllegalStateException.class, executable);
        assertEquals("Authentication is required", e.getMessage());
        verifyNoInteractions(partyConnectorMock, productsConnectorMock);
    }

    @Test
    void getInstitutionUserProducts_nullPrincipal(){
        //given
        String institutionId = "institutionId";
        TestSecurityContextHolder.setAuthentication(new TestingAuthenticationToken(null, null));
        //when
        Executable executable = () -> institutionService.getInstitutionUserProducts(institutionId);
        //then
        IllegalStateException e = assertThrows(IllegalStateException.class, executable);
        assertEquals("Not SelfCareUser principal", e.getMessage());
        verifyNoInteractions(partyConnectorMock, productsConnectorMock);
    }

    @Test
    void getInstitutionUserProducts_nullInstitutionId(){
        //given
        String institutionId = null;
        //when
        Executable executable = () -> institutionService.getInstitutionUserProducts(institutionId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_INSTITUTION_MESSAGE, e.getMessage());
        verifyNoInteractions(partyConnectorMock, productsConnectorMock);
    }

    @Test
    void getInstitutionUserProducts_nullProducts(){
        //given
        String institutionId = "institutionId";
        String userId = UUID.randomUUID().toString();
        final SelfCareUser selfCareUser = SelfCareUser.builder(userId)
                .email("test@example.com")
                .name("name")
                .surname("surname")
                .build();
        TestSecurityContextHolder.setAuthentication(new TestingAuthenticationToken(selfCareUser, null));

        //when
        List<Product> result = institutionService.getInstitutionUserProducts(institutionId);
        //then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productsConnectorMock, times(1))
                .getProducts();
        verifyNoMoreInteractions(partyConnectorMock);
        verifyNoInteractions(partyConnectorMock);
    }

    @Test
    void getInstitutionUserProducts() {
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

        EnumMap<PartyRole, it.pagopa.selfcare.external_api.model.product.ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class);
        for (PartyRole partyRole : PartyRole.values()) {
            it.pagopa.selfcare.external_api.model.product.ProductRoleInfo productRoleInfo = new it.pagopa.selfcare.external_api.model.product.ProductRoleInfo();
            List<it.pagopa.selfcare.external_api.model.product.ProductRoleInfo.ProductRole> roles = new ArrayList<>();
            roles.add(mockInstance(new it.pagopa.selfcare.external_api.model.product.ProductRoleInfo.ProductRole(), partyRole.ordinal() + 1));
            roles.add(mockInstance(new it.pagopa.selfcare.external_api.model.product.ProductRoleInfo.ProductRole(), partyRole.ordinal() + 2));
            productRoleInfo.setRoles(roles);
            productRoleInfo.setMultiroleAllowed(true);
            roleMappings.put(partyRole, productRoleInfo);
        }

        product1.setRoleMappings(roleMappings);
        product2.setRoleMappings(roleMappings);
        product3.setRoleMappings(roleMappings);
        product4.setRoleMappings(roleMappings);
        PartyProduct partyProduct1 = mockInstance(new PartyProduct(), 1);
        PartyProduct partyProduct2 = mockInstance(new PartyProduct(), 2);
        partyProduct2.setRole(PartyRole.OPERATOR);
        PartyProduct partyProduct3 = mockInstance(new PartyProduct(), 3);
        product1.setId(partyProduct1.getId());
        product2.setId(partyProduct2.getId());
        product3.setId("id3");
        product4.setId("id4");
        final List<PartyProduct> partyProducts = List.of(partyProduct1, partyProduct2, partyProduct3);
        final List<Product> products = List.of(product1, product2, product3, product4);
        when(partyConnectorMock.getInstitutionUserProducts(any(), any()))
                .thenReturn(partyProducts);
        when(productsConnectorMock.getProducts())
                .thenReturn(products);
        //when
        List<Product> result = institutionService.getInstitutionUserProducts(institutionId);
        //then
        assertEquals(2, result.size());
        verify(partyConnectorMock, times(1))
                .getInstitutionUserProducts(institutionId, userId);
        verify(productsConnectorMock, times(1))
                .getProducts();
        verifyNoMoreInteractions(partyConnectorMock, productsConnectorMock);
    }
    
    @Test
    void getInstitutionUserProducts_multipleRolesMerge() {
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
        final PartyProduct partyProduct1 = mockInstance(new PartyProduct(), 1);
        partyProduct1.setId("prod-io");
        partyProduct1.setRole(PartyRole.OPERATOR);
        final PartyProduct partyProduct2 = mockInstance(new PartyProduct(), 2);
        partyProduct2.setId("prod-io");
        partyProduct2.setRole(PartyRole.DELEGATE);
        final PartyProduct partyProduct3 = mockInstance(new PartyProduct(), 3);
        partyProduct3.setId("prod-interop");
        partyProduct3.setRole(PartyRole.OPERATOR);
        product1.setId(partyProduct1.getId());
        product2.setId(partyProduct3.getId());
        product3.setId("id3");
        product4.setId("id4");
        final List<PartyProduct> partyProducts = List.of(partyProduct1, partyProduct2, partyProduct3);
        final List<Product> products = List.of(product1, product2, product3, product4);
        when(partyConnectorMock.getInstitutionUserProducts(any(), any()))
                .thenReturn(partyProducts);
        when(productsConnectorMock.getProducts())
                .thenReturn(products);
        //when
        List<Product> result = institutionService.getInstitutionUserProducts(institutionId);
        //then
        assertEquals(2, result.size());
        verify(partyConnectorMock, times(1))
                .getInstitutionUserProducts(institutionId, userId);
        verify(productsConnectorMock, times(1))
                .getProducts();
        verifyNoMoreInteractions(partyConnectorMock, productsConnectorMock);
    }


    @Test
    void getInstitutionProductUsers_nullInstitutionId() {
        // given
        final String institutionId = null;
        final String productId = "productId";
        final Optional<String> userId = Optional.empty();
        final Optional<Set<String>> productRole = Optional.empty();
        // when
        Executable executable = () -> institutionService.getInstitutionProductUsers(institutionId, productId, userId, productRole, null);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals(REQUIRED_INSTITUTION_MESSAGE, e.getMessage());
        verifyNoInteractions(productsConnectorMock, partyConnectorMock);
    }


    @Test
    void getInstitutionProductUsers_nullProductId() {
        // given
        final String institutionId = "institutionId";
        final String productId = null;
        final Optional<String> userId = Optional.empty();
        final Optional<Set<String>> productRole = Optional.empty();
        // when
        Executable executable = () -> institutionService.getInstitutionProductUsers(institutionId, productId, userId, productRole, null);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("A Product id is required", e.getMessage());
        verifyNoInteractions(productsConnectorMock, partyConnectorMock);
    }


    @Test
    void getInstitutionProductUsers() {
        // given
        final String institutionId = "institutionId";
        final String productId = "productId";
        final Optional<String> usrIdParam = Optional.empty();
        final Optional<Set<String>> productRole = Optional.empty();
        final UserInfo userInfo = mockInstance(new UserInfo());
        final String userId = UUID.randomUUID().toString();
        userInfo.setId(userId);
        final User user = mockInstance(new User());
        user.setId(userId);
        WorkContact contact = mockInstance(new WorkContact());
        Map<String, WorkContact> workContact = new HashMap<>();
        workContact.put(institutionId, contact);
        user.setWorkContacts(workContact);
        when(partyConnectorMock.getUsers(any()))
                .thenReturn(Collections.singletonList(userInfo));
        when(userRegistryConnectorMock.getUserByInternalId(anyString(), any()))
                .thenReturn(user);
        // when
        Collection<UserInfo> userInfos = institutionService.getInstitutionProductUsers(institutionId, productId, usrIdParam, productRole, null);
        // then
        Assertions.assertNotNull(userInfos);
        userInfos.forEach(userInfo1 -> {
            TestUtils.checkNotNullFields(userInfo1, "products");
            TestUtils.checkNotNullFields(userInfo1.getUser());
        });
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(partyConnectorMock, times(1))
                .getUsers(filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertEquals(usrIdParam, capturedFilter.getRole());
        assertEquals(institutionId, capturedFilter.getInstitutionId().get());
        assertEquals(productId, capturedFilter.getProductId().get());
        assertEquals(productRole, capturedFilter.getProductRoles());
        assertEquals(Optional.empty(), capturedFilter.getUserId());
        assertEquals(Optional.of(EnumSet.of(ACTIVE)), capturedFilter.getAllowedStates());
        ArgumentCaptor<EnumSet<User.Fields>> filedsCaptor = ArgumentCaptor.forClass(EnumSet.class);
        verify(userRegistryConnectorMock, times(1))
                .getUserByInternalId(eq(userId), filedsCaptor.capture());
        EnumSet<User.Fields> capturedFields = filedsCaptor.getValue();
        assertTrue(capturedFields.contains(User.Fields.name));
        assertTrue(capturedFields.contains(User.Fields.familyName));
        assertTrue(capturedFields.contains(User.Fields.workContacts));
        assertFalse(capturedFields.contains(User.Fields.fiscalCode));
        verifyNoMoreInteractions(partyConnectorMock, userRegistryConnectorMock);
        verifyNoInteractions(productsConnectorMock);
    }

    @Test
    void getInstitutionProductUsers_case3(){
        // given
        final String institutionId = "institutionId";
        final String productId = "productId";
        final Optional<String> usrIdParam = Optional.empty();
        final String xSelfCareUid = "unregistered-interceptor";
        final Optional<Set<String>> productRole = Optional.empty();
        final UserInfo userInfo = mockInstance(new UserInfo());
        final String userId = UUID.randomUUID().toString();
        userInfo.setId(userId);
        final User user = mockInstance(new User());
        user.setId(userId);
        WorkContact contact = mockInstance(new WorkContact());
        Map<String, WorkContact> workContact = new HashMap<>();
        workContact.put(institutionId, contact);
        user.setWorkContacts(workContact);
        when(partyConnectorMock.getUsers(any()))
                .thenReturn(Collections.singletonList(userInfo));
        when(userRegistryConnectorMock.getUserByInternalId(anyString(), any()))
                .thenReturn(user);
        // when
        Collection<UserInfo> userInfos = institutionService.getInstitutionProductUsers(institutionId, productId, usrIdParam, productRole, xSelfCareUid);
        // then
        Assertions.assertNotNull(userInfos);
        userInfos.forEach(userInfo1 -> {
            TestUtils.checkNotNullFields(userInfo1, "products");
            TestUtils.checkNotNullFields(userInfo1.getUser());
        });
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(partyConnectorMock, times(1))
                .getUsers(filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertEquals(usrIdParam, capturedFilter.getRole());
        assertEquals(institutionId, capturedFilter.getInstitutionId().get());
        assertEquals(productId, capturedFilter.getProductId().get());
        assertEquals(productRole, capturedFilter.getProductRoles());
        assertEquals(Optional.empty(), capturedFilter.getUserId());
        assertEquals(Optional.of(EnumSet.of(ACTIVE)), capturedFilter.getAllowedStates());
        ArgumentCaptor<EnumSet<User.Fields>> filedsCaptor = ArgumentCaptor.forClass(EnumSet.class);
        verify(userRegistryConnectorMock, times(1))
                .getUserByInternalId(eq(userId), filedsCaptor.capture());
        EnumSet<User.Fields> capturedFields = filedsCaptor.getValue();
        assertTrue(capturedFields.contains(User.Fields.name));
        assertTrue(capturedFields.contains(User.Fields.familyName));
        assertTrue(capturedFields.contains(User.Fields.workContacts));
        assertFalse(capturedFields.contains(User.Fields.fiscalCode));
        verifyNoMoreInteractions(partyConnectorMock, userRegistryConnectorMock);
        verifyNoInteractions(productsConnectorMock);
    }

    @Test
    void getInstitutionProductUsers_withFiscalCode() {
        // given
        final String institutionId = "institutionId";
        final String productId = "productId";
        final Optional<String> usrIdParam = Optional.empty();
        final Optional<Set<String>> productRole = Optional.empty();
        final String xSelfCareUid = "onboarding-interceptor";
        final UserInfo userInfo = mockInstance(new UserInfo());
        final String userId = UUID.randomUUID().toString();
        userInfo.setId(userId);
        User user = mockInstance(new User());
        user.setId(userId);
        WorkContact contact = mockInstance(new WorkContact());
        Map<String, WorkContact> workContact = new HashMap<>();
        workContact.put(institutionId, contact);
        user.setWorkContacts(workContact);
        when(partyConnectorMock.getUsers(any()))
                .thenReturn(Collections.singletonList(userInfo));
        when(userRegistryConnectorMock.getUserByInternalId(anyString(), any()))
                .thenReturn(user);
        // when
        Collection<UserInfo> userInfos = institutionService.getInstitutionProductUsers(institutionId, productId, usrIdParam, productRole, xSelfCareUid);
        // then
        Assertions.assertNotNull(userInfos);
        userInfos.forEach(userInfo1 -> {
            TestUtils.checkNotNullFields(userInfo1, "products");
            TestUtils.checkNotNullFields(userInfo1.getUser());
        });
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(partyConnectorMock, times(1))
                .getUsers(filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertEquals(usrIdParam, capturedFilter.getRole());
        assertEquals(institutionId, capturedFilter.getInstitutionId().get());
        assertEquals(productId, capturedFilter.getProductId().get());
        assertEquals(productRole, capturedFilter.getProductRoles());
        assertEquals(Optional.empty(), capturedFilter.getUserId());
        assertEquals(Optional.of(EnumSet.of(ACTIVE)), capturedFilter.getAllowedStates());
        ArgumentCaptor<EnumSet<User.Fields>> filedsCaptor = ArgumentCaptor.forClass(EnumSet.class);
        verify(userRegistryConnectorMock, times(1))
                .getUserByInternalId(eq(userId), filedsCaptor.capture());
        EnumSet<User.Fields> capturedFields = filedsCaptor.getValue();
        assertTrue(capturedFields.contains(User.Fields.name));
        assertTrue(capturedFields.contains(User.Fields.familyName));
        assertTrue(capturedFields.contains(User.Fields.workContacts));
        assertTrue(capturedFields.contains(User.Fields.fiscalCode));
        verifyNoMoreInteractions(partyConnectorMock, userRegistryConnectorMock);
        verifyNoInteractions(productsConnectorMock);
    }

    @Test
    void getGeographicTaxonomyList() {
        // given
        final String institutionId = "institutionId";
        final Institution institutionMock = mockInstance(new Institution());
        institutionMock.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        when(partyConnectorMock.getGeographicTaxonomyList(anyString()))
                .thenReturn(institutionMock.getGeographicTaxonomies());
        // when
        List<GeographicTaxonomy> result = institutionService.getGeographicTaxonomyList(institutionId);
        // then
        assertNotNull(result);
        assertEquals(institutionMock.getGeographicTaxonomies().get(0).getCode(), result.get(0).getCode());
        assertEquals(institutionMock.getGeographicTaxonomies().get(0).getDesc(), result.get(0).getDesc());
        verify(partyConnectorMock, times(1))
                .getGeographicTaxonomyList(institutionId);
        verifyNoMoreInteractions(partyConnectorMock);
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
        verifyNoInteractions(partyConnectorMock);
    }

    @Test
    void getInstitutionsByGeoTaxonomies() {
        //given
        final Set<String> geoTaxIds = Set.of("geoTax1", "geoTax2");
        final SearchMode searchMode = SearchMode.any;
        when(partyConnectorMock.getInstitutionsByGeoTaxonomies(anyString(), any()))
                .thenReturn(List.of(mockInstance(new Institution())));
        //when
        Collection<Institution> results = institutionService.getInstitutionsByGeoTaxonomies(geoTaxIds, searchMode);
        //then
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        ArgumentCaptor<String> geoTaxIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(partyConnectorMock, times(1))
                .getInstitutionsByGeoTaxonomies(geoTaxIdCaptor.capture(), eq(searchMode));
        assertEquals(String.join(",", geoTaxIds), geoTaxIdCaptor.getValue());
        verifyNoInteractions(userRegistryConnectorMock, productsConnectorMock);
        verifyNoMoreInteractions(partyConnectorMock);
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
        verifyNoInteractions(userRegistryConnectorMock, productsConnectorMock, partyConnectorMock);
    }
    @Test
    void addInstitution_exists() {
        //given
        CreatePnPgInstitution createPnPgInstitution = mockInstance(new CreatePnPgInstitution());
        Institution institution = mockInstance(new Institution());
        when(partyConnectorMock.getInstitutionByExternalId(anyString()))
                .thenReturn(institution);
        //when
        String internalInstitutionId = institutionService.addInstitution(createPnPgInstitution);
        //then
        assertEquals(institution.getId(), internalInstitutionId);
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(createPnPgInstitution.getExternalId());
        verifyNoMoreInteractions(partyConnectorMock);
    }

    @Test
    void addInstitution_notExists() {
        //given
        final CreatePnPgInstitution createPnPgInstitution = mockInstance(new CreatePnPgInstitution());
        final String institutionId = "institutionId";
        doThrow(ResourceNotFoundException.class).
                when(partyConnectorMock)
                .getInstitutionByExternalId(anyString());
        when(msCoreConnectorMock.createPnPgInstitution(any()))
                .thenReturn(institutionId);
        //when
        String internalInstitutionId = institutionService.addInstitution(createPnPgInstitution);
        //then
        assertEquals(institutionId, internalInstitutionId);
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(createPnPgInstitution.getExternalId());
        verify(msCoreConnectorMock, times(1))
                .createPnPgInstitution(createPnPgInstitution);
        verifyNoMoreInteractions(partyConnectorMock);
    }
}
