package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.external_api.api.PartyConnector;
import it.pagopa.selfcare.external_api.api.ProductsConnector;
import it.pagopa.selfcare.external_api.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.external_api.model.product.PartyProduct;
import it.pagopa.selfcare.external_api.model.product.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.test.context.TestSecurityContextHolder;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstitutionServiceImplTest {
    @InjectMocks
    private InstitutionServiceImpl institutionService;

    @Mock
    private PartyConnector partyConnectorMock;

    @Mock
    private ProductsConnector productsConnectorMock;
    
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
        partyProduct2.setRole(PartyRole.OPERATOR);
        PartyProduct partyProduct3 = mockInstance(new PartyProduct(), 3);
        partyProduct3.setId("prod-interop");
        partyProduct3.setRole(PartyRole.OPERATOR);
        PartyProduct partyProduct4 = mockInstance(new PartyProduct(), 4);
        partyProduct4.setId("prod-interop");
        partyProduct4.setRole(PartyRole.OPERATOR);
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
}
