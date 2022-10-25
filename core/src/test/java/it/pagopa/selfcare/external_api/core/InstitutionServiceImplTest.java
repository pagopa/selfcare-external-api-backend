package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.external_api.api.PartyConnector;
import it.pagopa.selfcare.external_api.api.ProductsConnector;
import it.pagopa.selfcare.external_api.api.UserRegistryConnector;
import it.pagopa.selfcare.external_api.model.institutions.InstitutionInfo;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.test.context.TestSecurityContextHolder;

import java.util.*;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.external_api.model.user.RelationshipState.ACTIVE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstitutionServiceImplTest {
    @InjectMocks
    private InstitutionServiceImpl institutionService;

    @Mock
    private PartyConnector partyConnectorMock;

    @Mock
    private ProductsConnector productsConnectorMock;

    @Mock
    private UserRegistryConnector userRegistryConnectorMock;

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
        assertEquals("An institutionId is required", e.getMessage());
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
    void getInstitutionUserProducts(){
        //given
        final String institutionId = "institutionId";
        final String userId = UUID.randomUUID().toString();
        final SelfCareUser selfCareUser = SelfCareUser.builder(userId)
                .email("test@example.com")
                .name("name")
                .surname("surname")
                .build();
        TestSecurityContextHolder.setAuthentication(new TestingAuthenticationToken(selfCareUser, null));
        Product product1 = mockInstance(new Product(), 1);
        Product product2 = mockInstance(new Product(), 2);
        Product product3 = mockInstance(new Product(), 3);
        Product product4 = mockInstance(new Product(), 4);

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
    void getInstitutionUserProducts_multipleRolesMerge(){
        //given
        final String institutionId = "institutionId";
        final String userId = UUID.randomUUID().toString();
        final SelfCareUser selfCareUser = SelfCareUser.builder(userId)
                .email("test@example.com")
                .name("name")
                .surname("surname")
                .build();
        TestSecurityContextHolder.setAuthentication(new TestingAuthenticationToken(selfCareUser, null));
        Product product1 = mockInstance(new Product(), 1);
        Product product2 = mockInstance(new Product(), 2);
        Product product3 = mockInstance(new Product(), 3);
        Product product4 = mockInstance(new Product(), 4);
        PartyProduct partyProduct1 = mockInstance(new PartyProduct(), 1);
        partyProduct1.setId("prod-io");
        partyProduct1.setRole(PartyRole.OPERATOR);
        PartyProduct partyProduct2 = mockInstance(new PartyProduct(), 2);
        partyProduct2.setId("prod-io");
        partyProduct2.setRole(PartyRole.DELEGATE);
        PartyProduct partyProduct3 = mockInstance(new PartyProduct(), 3);
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
        String institutionId = null;
        String productId = "productId";
        Optional<String> userId = Optional.empty();
        Optional<Set<String>> productRole = Optional.empty();
        // when
        Executable executable = () -> institutionService.getInstitutionProductUsers(institutionId, productId, userId, productRole);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("An Institution id is required", e.getMessage());
        verifyNoInteractions(productsConnectorMock, partyConnectorMock);
    }


    @Test
    void getInstitutionProductUsers_nullProductId() {
        // given
        String institutionId = "institutionId";
        String productId = null;
        Optional<String> userId = Optional.empty();
        Optional<Set<String>> productRole = Optional.empty();
        // when
        Executable executable = () -> institutionService.getInstitutionProductUsers(institutionId, productId, userId, productRole);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("A Product id is required", e.getMessage());
        verifyNoInteractions(productsConnectorMock, partyConnectorMock);
    }


    @Test
    void getInstitutionProductUsers() {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        Optional<String> usrIdParam = Optional.empty();
        Optional<Set<String>> productRole = Optional.empty();
        UserInfo userInfo = mockInstance(new UserInfo());
        String userId = UUID.randomUUID().toString();
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
        Collection<UserInfo> userInfos = institutionService.getInstitutionProductUsers(institutionId, productId, usrIdParam, productRole);
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
        verifyNoMoreInteractions(partyConnectorMock, userRegistryConnectorMock);
        verifyNoInteractions(productsConnectorMock);
    }

}
