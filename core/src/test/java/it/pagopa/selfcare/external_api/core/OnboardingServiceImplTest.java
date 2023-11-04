package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import it.pagopa.selfcare.external_api.api.MsPartyRegistryProxyConnector;
import it.pagopa.selfcare.external_api.api.PartyConnector;
import it.pagopa.selfcare.external_api.api.ProductsConnector;
import it.pagopa.selfcare.external_api.api.UserRegistryConnector;
import it.pagopa.selfcare.external_api.core.exception.OnboardingNotAllowedException;
import it.pagopa.selfcare.external_api.core.exception.UpdateNotAllowedException;
import it.pagopa.selfcare.external_api.core.strategy.OnboardingValidationStrategy;
import it.pagopa.selfcare.external_api.exceptions.InstitutionAlreadyOnboardedException;
import it.pagopa.selfcare.external_api.exceptions.InstitutionDoesNotExistException;
import it.pagopa.selfcare.external_api.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.external_api.model.institutions.GeographicTaxonomy;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.institutions.InstitutionResource;
import it.pagopa.selfcare.external_api.model.onboarding.User;
import it.pagopa.selfcare.external_api.model.onboarding.*;
import it.pagopa.selfcare.external_api.model.product.Product;
import it.pagopa.selfcare.external_api.model.product.ProductRoleInfo;
import it.pagopa.selfcare.external_api.model.product.ProductStatus;
import it.pagopa.selfcare.external_api.model.relationship.Relationship;
import it.pagopa.selfcare.external_api.model.relationship.Relationships;
import it.pagopa.selfcare.external_api.model.user.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.TestSecurityContextHolder;

import javax.validation.ValidationException;
import java.util.*;

import static it.pagopa.selfcare.commons.utils.TestUtils.checkNotNullFields;
import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.external_api.core.OnboardingServiceImpl.*;
import static it.pagopa.selfcare.external_api.model.user.User.Fields.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OnboardingServiceImplTest {

    @InjectMocks
    private OnboardingServiceImpl onboardingServiceImpl;

    @Mock
    private PartyConnector partyConnectorMock;

    @Mock
    private ProductsConnector productsConnectorMock;

    @Mock
    private OnboardingValidationStrategy onboardingValidationStrategyMock;

    @Mock
    private UserRegistryConnector userRegistryConnectorMock;

    @Mock
    private MsPartyRegistryProxyConnector msPartyRegistryProxyConnectorMock;

    @Captor
    private ArgumentCaptor<OnboardingImportData> onboardingImportDataCaptor;

    @Captor
    private ArgumentCaptor<OnboardingData> onboardingDataCaptor;

    private final static User dummyManager;
    private final static User dummyDelegate;

    @BeforeEach
    void beforeEach() {
        TestSecurityContextHolder.clearContext();
    }

    static {
        dummyManager = new User();
        dummyManager.setEmail("manager@pec.it");
        dummyManager.setName("manager");
        dummyManager.setSurname("manager");
        dummyManager.setTaxCode("manager");
        dummyManager.setRole(PartyRole.MANAGER);


        dummyDelegate = new User();
        dummyDelegate.setEmail("delegate@pec.it");
        dummyDelegate.setName("delegate");
        dummyDelegate.setSurname("delegate");
        dummyDelegate.setTaxCode("delegate");
        dummyDelegate.setRole(PartyRole.DELEGATE);
    }

    @Test
    void olContractOnboarding_institutionAlreadyOnboardedException() {
        // given
        OnboardingImportData onboardingImportData = mockInstance(new OnboardingImportData());
        ResponseEntity<Void> responseEntityMock = new ResponseEntity<>(HttpStatus.NO_CONTENT);
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenReturn(responseEntityMock);
        // when
        Executable executable = () -> onboardingServiceImpl.oldContractOnboarding(onboardingImportData);
        // then
        InstitutionAlreadyOnboardedException e = assertThrows(InstitutionAlreadyOnboardedException.class, executable);
        assertEquals(String.format("The institution with external id %s is already onboarded on the product %s",
                        onboardingImportData.getInstitutionExternalId(),
                        onboardingImportData.getProductId()),
                e.getMessage());
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingImportData.getInstitutionExternalId(), onboardingImportData.getProductId());
        verifyNoMoreInteractions(partyConnectorMock);
        verifyNoInteractions(userRegistryConnectorMock, productsConnectorMock);
    }

    @Test
    void olContractOnboarding_noExceptionsRaised() {
        // given
        OnboardingImportData onboardingImportData = mockInstance(new OnboardingImportData());
        ResponseEntity<Void> responseEntityMock = new ResponseEntity<>(HttpStatus.OK);
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenReturn(responseEntityMock);
        // when
        Executable executable = () -> onboardingServiceImpl.oldContractOnboarding(onboardingImportData);
        // then
        assertDoesNotThrow(executable);
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingImportData.getInstitutionExternalId(), onboardingImportData.getProductId());
        verifyNoMoreInteractions(partyConnectorMock);
        verifyNoInteractions(userRegistryConnectorMock, productsConnectorMock);
    }

    @Test
    void oldContractOnboarding_nullOnboardingData() {
        // given
        OnboardingImportData onboardingImportData = null;
        // when
        Executable executable = () -> onboardingServiceImpl.oldContractOnboarding(onboardingImportData);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_ONBOARDING_DATA_MESSAGE, e.getMessage());
        verifyNoInteractions(partyConnectorMock, productsConnectorMock, userRegistryConnectorMock, onboardingValidationStrategyMock);
    }

    @Test
    void oldContractOnboarding_nullRoleMapping() {
        // given
        OnboardingImportData onboardingImportData = mockInstance(new OnboardingImportData());
        Product product = mockInstance(new Product(), "setId");
        product.setId(onboardingImportData.getProductId());
        Institution institutionMock = mockInstance(new Institution(), "setExternalId");
        institutionMock.setExternalId(onboardingImportData.getInstitutionExternalId());
        institutionMock.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        InstitutionResource institutionResourceMock = mockInstance(new InstitutionResource(), "setCategory");
        institutionResourceMock.setCategory("L6");
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenThrow(InstitutionDoesNotExistException.class);
        when(msPartyRegistryProxyConnectorMock.findInstitution(anyString()))
                .thenReturn(institutionResourceMock);
        when(partyConnectorMock.getInstitutionByExternalId(any()))
                .thenReturn(institutionMock);
        when(productsConnectorMock.getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType()))
                .thenReturn(product);
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        // when
        Executable executable = () -> onboardingServiceImpl.oldContractOnboarding(onboardingImportData);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("Role mappings is required", e.getMessage());
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingImportData.getInstitutionExternalId(), onboardingImportData.getProductId());
        verify(msPartyRegistryProxyConnectorMock, times(1))
                .findInstitution(onboardingImportData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingImportData.getInstitutionExternalId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingImportData.getProductId(), onboardingImportData.getInstitutionExternalId());
        verifyNoMoreInteractions(productsConnectorMock, onboardingValidationStrategyMock, partyConnectorMock, msPartyRegistryProxyConnectorMock);
        verifyNoInteractions(userRegistryConnectorMock);
    }

    @Test
    void olContractOnboarding_productPhaseOutException() {
        // given
        OnboardingImportData onboardingImportData = mockInstance(new OnboardingImportData());
        Product product = mockInstance(new Product(), "setId");
        product.setId(onboardingImportData.getProductId());
        product.setStatus(ProductStatus.PHASE_OUT);
        Institution institutionMock = mockInstance(new Institution(), "setExternalId");
        institutionMock.setExternalId(onboardingImportData.getInstitutionExternalId());
        institutionMock.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        InstitutionResource institutionResourceMock = mockInstance(new InstitutionResource(), "setCategory");
        institutionResourceMock.setCategory("L6");
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenThrow(InstitutionDoesNotExistException.class);
        when(msPartyRegistryProxyConnectorMock.findInstitution(anyString()))
                .thenReturn(institutionResourceMock);
        when(partyConnectorMock.getInstitutionByExternalId(any()))
                .thenReturn(institutionMock);
        when(productsConnectorMock.getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType()))
                .thenReturn(product);
        // when
        Executable executable = () -> onboardingServiceImpl.oldContractOnboarding(onboardingImportData);
        // then
        ValidationException e = assertThrows(ValidationException.class, executable);
        assertEquals(String.format("Unable to complete the onboarding for institution with external id '%s' to product '%s', the product is dismissed.",
                        onboardingImportData.getInstitutionExternalId(),
                        product.getId()),
                e.getMessage());
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingImportData.getInstitutionExternalId(), onboardingImportData.getProductId());
        verify(msPartyRegistryProxyConnectorMock, times(1))
                .findInstitution(onboardingImportData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingImportData.getInstitutionExternalId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType());
        verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock, msPartyRegistryProxyConnectorMock);
        verifyNoInteractions(userRegistryConnectorMock);
    }

    @Test
    void oldContractOnboarding_notAllowed() {
        // given
        User userInfo = mockInstance(new User(), "setRole");
        userInfo.setRole(PartyRole.MANAGER);
        OnboardingImportData onboardingImportData = mockInstance(new OnboardingImportData(), "setUsers", "setBilling", "setInstitutionUpdate", "setPricingPlan");
        onboardingImportData.setBilling(new Billing());
        onboardingImportData.setInstitutionUpdate(new InstitutionUpdate());
        onboardingImportData.setUsers(List.of(userInfo));
        Product product = mockInstance(new Product(), "setId", "setParentId");
        product.setId(onboardingImportData.getProductId());
        product.setRoleMappings(new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, null);
        }});
        Institution institutionMock = mockInstance(new Institution(), "setExternalId");
        institutionMock.setExternalId(onboardingImportData.getInstitutionExternalId());
        institutionMock.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        InstitutionResource institutionResourceMock = mockInstance(new InstitutionResource(), "setCategory");
        institutionResourceMock.setCategory("L6");
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenThrow(InstitutionDoesNotExistException.class);
        when(msPartyRegistryProxyConnectorMock.findInstitution(anyString()))
                .thenReturn(institutionResourceMock);
        when(partyConnectorMock.getInstitutionByExternalId(any()))
                .thenReturn(institutionMock);
        when(productsConnectorMock.getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType()))
                .thenReturn(product);
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(false);
        // when
        Executable executable = () -> onboardingServiceImpl.oldContractOnboarding(onboardingImportData);
        // then
        Exception e = assertThrows(OnboardingNotAllowedException.class, executable);
        assertEquals("Institution with external id '" + onboardingImportData.getInstitutionExternalId() + "' is not allowed to onboard '" + onboardingImportData.getProductId() + "' product",
                e.getMessage());
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingImportData.getInstitutionExternalId(), onboardingImportData.getProductId());
        verify(msPartyRegistryProxyConnectorMock, times(1))
                .findInstitution(onboardingImportData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingImportData.getInstitutionExternalId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingImportData.getProductId(), onboardingImportData.getInstitutionExternalId());
        verifyNoMoreInteractions(productsConnectorMock, onboardingValidationStrategyMock, partyConnectorMock, msPartyRegistryProxyConnectorMock);
        verifyNoInteractions(userRegistryConnectorMock);
    }

    @Test
    void oldContractOnboarding_nullProductRoles() {
        // given
        User userInfo = mockInstance(new User(), "setRole");
        userInfo.setRole(PartyRole.MANAGER);
        OnboardingImportData onboardingImportData = mockInstance(new OnboardingImportData(), "setUsers", "setBilling", "setInstitutionUpdate", "setPricingPlan");
        onboardingImportData.setBilling(new Billing());
        onboardingImportData.setInstitutionUpdate(new InstitutionUpdate());
        onboardingImportData.setUsers(List.of(userInfo));
        Product product = mockInstance(new Product(), "setParentId", "setId");
        product.setId(onboardingImportData.getProductId());
        product.setRoleMappings(new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, null);
        }});
        Institution institutionMock = mockInstance(new Institution(), "setExternalId");
        institutionMock.setExternalId(onboardingImportData.getInstitutionExternalId());
        institutionMock.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        InstitutionResource institutionResourceMock = mockInstance(new InstitutionResource(), "setCategory");
        institutionResourceMock.setCategory("L6");
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenThrow(InstitutionDoesNotExistException.class);
        when(msPartyRegistryProxyConnectorMock.findInstitution(anyString()))
                .thenReturn(institutionResourceMock);
        when(partyConnectorMock.getInstitutionByExternalId(any()))
                .thenReturn(institutionMock);
        when(productsConnectorMock.getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType()))
                .thenReturn(product);
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        // when
        Executable executable = () -> onboardingServiceImpl.oldContractOnboarding(onboardingImportData);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(String.format(ATLEAST_ONE_PRODUCT_ROLE_REQUIRED, userInfo.getRole()), e.getMessage());
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingImportData.getInstitutionExternalId(), onboardingImportData.getProductId());
        verify(msPartyRegistryProxyConnectorMock, times(1))
                .findInstitution(onboardingImportData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingImportData.getInstitutionExternalId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingImportData.getProductId(), onboardingImportData.getInstitutionExternalId());
        verifyNoMoreInteractions(productsConnectorMock, onboardingValidationStrategyMock, partyConnectorMock, msPartyRegistryProxyConnectorMock);
        verifyNoInteractions(userRegistryConnectorMock);
    }

    @Test
    void oldContractOnboarding_emptyProductRoles() {
        // given
        User userInfo = mockInstance(new User(), "setRole");
        userInfo.setRole(PartyRole.DELEGATE);
        OnboardingImportData onboardingImportData = mockInstance(new OnboardingImportData(), "setUsers", "setBilling", "setInstitutionUpdate", "setPricingPlan");
        onboardingImportData.setBilling(new Billing());
        onboardingImportData.setInstitutionUpdate(new InstitutionUpdate());
        onboardingImportData.setUsers(List.of(userInfo));
        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId", "setId");
        productMock.setId(onboardingImportData.getProductId());
        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        productRoleInfo1.setRoles(List.of(mockInstance(new ProductRoleInfo.ProductRole(), 1)));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        productRoleInfo2.setRoles(List.of());
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        productMock.setRoleMappings(roleMappings);
        Institution institutionMock = mockInstance(new Institution(), "setExternalId");
        institutionMock.setExternalId(onboardingImportData.getInstitutionExternalId());
        institutionMock.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        InstitutionResource institutionResourceMock = mockInstance(new InstitutionResource(), "setCategory");
        institutionResourceMock.setCategory("L6");
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenThrow(InstitutionDoesNotExistException.class);
        when(msPartyRegistryProxyConnectorMock.findInstitution(anyString()))
                .thenReturn(institutionResourceMock);
        when(partyConnectorMock.getInstitutionByExternalId(any()))
                .thenReturn(institutionMock);
        when(productsConnectorMock.getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType()))
                .thenReturn(productMock);
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        // when
        Executable executable = () -> onboardingServiceImpl.oldContractOnboarding(onboardingImportData);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(String.format(ATLEAST_ONE_PRODUCT_ROLE_REQUIRED, userInfo.getRole()), e.getMessage());
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingImportData.getInstitutionExternalId(), onboardingImportData.getProductId());
        verify(msPartyRegistryProxyConnectorMock, times(1))
                .findInstitution(onboardingImportData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingImportData.getInstitutionExternalId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingImportData.getProductId(), onboardingImportData.getInstitutionExternalId());
        verifyNoMoreInteractions(productsConnectorMock, onboardingValidationStrategyMock, partyConnectorMock, msPartyRegistryProxyConnectorMock);
        verifyNoInteractions(userRegistryConnectorMock);
    }

    @Test
    void oldContractOnboarding_MoreThanOneProductRoles() {
        // given
        User userInfo = mockInstance(new User(), "setRole");
        userInfo.setRole(PartyRole.DELEGATE);
        OnboardingImportData onboardingImportData = mockInstance(new OnboardingImportData(), "setUsers", "setBilling", "setInstitutionUpdate", "setPricingPlan");
        onboardingImportData.setBilling(new Billing());
        onboardingImportData.setInstitutionUpdate(new InstitutionUpdate());
        onboardingImportData.setUsers(List.of(userInfo));
        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId", "setId");
        productMock.setId(onboardingImportData.getProductId());
        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        productRoleInfo1.setRoles(List.of(mockInstance(new ProductRoleInfo.ProductRole(), 1)));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        productRoleInfo2.setRoles(List.of(mockInstance(new ProductRoleInfo.ProductRole(), 1),
                mockInstance(new ProductRoleInfo.ProductRole(), 2)));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        productMock.setRoleMappings(roleMappings);
        Institution institutionMock = mockInstance(new Institution(), "setExternalId");
        institutionMock.setExternalId(onboardingImportData.getInstitutionExternalId());
        institutionMock.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        InstitutionResource institutionResourceMock = mockInstance(new InstitutionResource(), "setCategory");
        institutionResourceMock.setCategory("L6");
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenThrow(InstitutionDoesNotExistException.class);
        when(msPartyRegistryProxyConnectorMock.findInstitution(anyString()))
                .thenReturn(institutionResourceMock);
        when(partyConnectorMock.getInstitutionByExternalId(any()))
                .thenReturn(institutionMock);
        when(productsConnectorMock.getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType()))
                .thenReturn(productMock);
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        // when
        Executable executable = () -> onboardingServiceImpl.oldContractOnboarding(onboardingImportData);
        // then
        IllegalStateException e = assertThrows(IllegalStateException.class, executable);
        assertEquals(String.format(MORE_THAN_ONE_PRODUCT_ROLE_AVAILABLE, userInfo.getRole()), e.getMessage());
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingImportData.getInstitutionExternalId(), onboardingImportData.getProductId());
        verify(msPartyRegistryProxyConnectorMock, times(1))
                .findInstitution(onboardingImportData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingImportData.getInstitutionExternalId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingImportData.getProductId(), onboardingImportData.getInstitutionExternalId());
        verifyNoMoreInteractions(productsConnectorMock, onboardingValidationStrategyMock, partyConnectorMock, msPartyRegistryProxyConnectorMock);
        verifyNoInteractions(userRegistryConnectorMock);
    }

    @Test
    void oldContactOnboarding_institutionExists() {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);
        OnboardingImportData onboardingImportData = mockInstance(new OnboardingImportData(), "setUsers", "setBilling", "setInstitutionUpdate", "setPricingPlan");
        onboardingImportData.setBilling(new Billing());
        onboardingImportData.setInstitutionUpdate(new InstitutionUpdate());
        onboardingImportData.setUsers(List.of(userInfo1, userInfo2));
        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId", "setId");
        productMock.setId(onboardingImportData.getProductId());
        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRoleInfo.ProductRole productRole1 = mockInstance(new ProductRoleInfo.ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRoleInfo.ProductRole productRole2 = mockInstance(new ProductRoleInfo.ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        InstitutionResource institutionResourceMock = mockInstance(new InstitutionResource(), "setCategory", "setTaxCode");
        institutionResourceMock.setCategory("L6");
        institutionResourceMock.setTaxCode(onboardingImportData.getInstitutionExternalId());
        Relationships relationshipsMock = mockInstance(new Relationships());
        relationshipsMock.setItems(List.of(mockInstance(new Relationship(), "setId")));
        relationshipsMock.getItems().get(0).setId(institution.getId());
        when(partyConnectorMock.getInstitutionByExternalId(anyString()))
                .thenReturn(institution);
        when(msPartyRegistryProxyConnectorMock.findInstitution(anyString()))
                .thenReturn(institutionResourceMock);
        productMock.setRoleMappings(roleMappings);
        when(productsConnectorMock.getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType()))
                .thenReturn(productMock);
        when(userRegistryConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenThrow(InstitutionDoesNotExistException.class);
        when(partyConnectorMock.getRelationships(any()))
                .thenReturn(relationshipsMock);
        // when
        onboardingServiceImpl.oldContractOnboarding(onboardingImportData);
        // then
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingImportData.getInstitutionExternalId(), onboardingImportData.getProductId());
        verify(msPartyRegistryProxyConnectorMock, times(1))
                .findInstitution(onboardingImportData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingImportData.getInstitutionExternalId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingImportData.getProductId(), onboardingImportData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .oldContractOnboardingOrganization(onboardingImportDataCaptor.capture());
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        onboardingImportData.getUsers().forEach(user ->
                verify(userRegistryConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verify(userRegistryConnectorMock, times(2))
                .saveUser(saveUserCaptor.capture());
        List<SaveUserDto> savedUsers = saveUserCaptor.getAllValues();
        savedUsers.forEach(saveUserDto -> assertTrue(saveUserDto.getWorkContacts().containsKey(institution.getId())));
        OnboardingImportData captured = onboardingImportDataCaptor.getValue();
        assertNotNull(captured.getUsers());
        assertEquals(2, captured.getUsers().size());
        captured.getUsers().forEach(userInfo -> {
            assertEquals(productRole, userInfo.getProductRole());
            assertNotNull(userInfo.getId());
        });
        assertEquals(onboardingImportData.getBilling(), relationshipsMock.getItems().get(0).getBilling());
        verify(partyConnectorMock, times(1))
                .getRelationships(institution.getId());
        verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock, userRegistryConnectorMock, onboardingValidationStrategyMock, msPartyRegistryProxyConnectorMock);
    }

    @Test
    void oldContactOnboarding_institutionExists_nullRelationships() {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);
        OnboardingImportData onboardingImportData = mockInstance(new OnboardingImportData(), "setUsers", "setBilling", "setInstitutionUpdate", "setPricingPlan");
        onboardingImportData.setBilling(new Billing());
        onboardingImportData.setInstitutionUpdate(new InstitutionUpdate());
        onboardingImportData.setUsers(List.of(userInfo1, userInfo2));
        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId", "setId");
        productMock.setId(onboardingImportData.getProductId());
        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRoleInfo.ProductRole productRole1 = mockInstance(new ProductRoleInfo.ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRoleInfo.ProductRole productRole2 = mockInstance(new ProductRoleInfo.ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        InstitutionResource institutionResourceMock = mockInstance(new InstitutionResource(), "setCategory", "setTaxCode");
        institutionResourceMock.setCategory("L6");
        institutionResourceMock.setTaxCode(onboardingImportData.getInstitutionExternalId());
        Relationships relationshipsMock = mockInstance(new Relationships());
        when(partyConnectorMock.getInstitutionByExternalId(anyString()))
                .thenReturn(institution);
        when(msPartyRegistryProxyConnectorMock.findInstitution(anyString()))
                .thenReturn(institutionResourceMock);
        productMock.setRoleMappings(roleMappings);
        when(productsConnectorMock.getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType()))
                .thenReturn(productMock);
        when(userRegistryConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenThrow(InstitutionDoesNotExistException.class);
        when(partyConnectorMock.getRelationships(any()))
                .thenReturn(relationshipsMock);
        // when
        onboardingServiceImpl.oldContractOnboarding(onboardingImportData);
        // then
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingImportData.getInstitutionExternalId(), onboardingImportData.getProductId());
        verify(msPartyRegistryProxyConnectorMock, times(1))
                .findInstitution(onboardingImportData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingImportData.getInstitutionExternalId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingImportData.getProductId(), onboardingImportData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .oldContractOnboardingOrganization(onboardingImportDataCaptor.capture());
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        onboardingImportData.getUsers().forEach(user ->
                verify(userRegistryConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verify(userRegistryConnectorMock, times(2))
                .saveUser(saveUserCaptor.capture());
        List<SaveUserDto> savedUsers = saveUserCaptor.getAllValues();
        savedUsers.forEach(saveUserDto -> assertTrue(saveUserDto.getWorkContacts().containsKey(institution.getId())));
        OnboardingImportData captured = onboardingImportDataCaptor.getValue();
        assertNotNull(captured.getUsers());
        assertEquals(2, captured.getUsers().size());
        captured.getUsers().forEach(userInfo -> {
            assertEquals(productRole, userInfo.getProductRole());
            assertNotNull(userInfo.getId());
        });
        verify(partyConnectorMock, times(1))
                .getRelationships(institution.getId());
        verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock, userRegistryConnectorMock, onboardingValidationStrategyMock, msPartyRegistryProxyConnectorMock);
    }

    @Test
    void oldContactOnboarding_institutionExists_emptyRelationships() {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);
        OnboardingImportData onboardingImportData = mockInstance(new OnboardingImportData(), "setUsers", "setBilling", "setInstitutionUpdate", "setPricingPlan");
        onboardingImportData.setBilling(new Billing());
        onboardingImportData.setInstitutionUpdate(new InstitutionUpdate());
        onboardingImportData.setUsers(List.of(userInfo1, userInfo2));
        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId", "setId");
        productMock.setId(onboardingImportData.getProductId());
        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRoleInfo.ProductRole productRole1 = mockInstance(new ProductRoleInfo.ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRoleInfo.ProductRole productRole2 = mockInstance(new ProductRoleInfo.ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        InstitutionResource institutionResourceMock = mockInstance(new InstitutionResource(), "setCategory", "setTaxCode");
        institutionResourceMock.setCategory("L6");
        institutionResourceMock.setTaxCode(onboardingImportData.getInstitutionExternalId());
        Relationships relationshipsMock = mockInstance(new Relationships());
        relationshipsMock.setItems(Collections.emptyList());
        when(partyConnectorMock.getInstitutionByExternalId(anyString()))
                .thenReturn(institution);
        when(msPartyRegistryProxyConnectorMock.findInstitution(anyString()))
                .thenReturn(institutionResourceMock);
        productMock.setRoleMappings(roleMappings);
        when(productsConnectorMock.getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType()))
                .thenReturn(productMock);
        when(userRegistryConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenThrow(InstitutionDoesNotExistException.class);
        when(partyConnectorMock.getRelationships(any()))
                .thenReturn(relationshipsMock);
        // when
        onboardingServiceImpl.oldContractOnboarding(onboardingImportData);
        // then
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingImportData.getInstitutionExternalId(), onboardingImportData.getProductId());
        verify(msPartyRegistryProxyConnectorMock, times(1))
                .findInstitution(onboardingImportData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingImportData.getInstitutionExternalId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingImportData.getProductId(), onboardingImportData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .oldContractOnboardingOrganization(onboardingImportDataCaptor.capture());
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        onboardingImportData.getUsers().forEach(user ->
                verify(userRegistryConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verify(userRegistryConnectorMock, times(2))
                .saveUser(saveUserCaptor.capture());
        List<SaveUserDto> savedUsers = saveUserCaptor.getAllValues();
        savedUsers.forEach(saveUserDto -> assertTrue(saveUserDto.getWorkContacts().containsKey(institution.getId())));
        OnboardingImportData captured = onboardingImportDataCaptor.getValue();
        assertNotNull(captured.getUsers());
        assertEquals(2, captured.getUsers().size());
        captured.getUsers().forEach(userInfo -> {
            assertEquals(productRole, userInfo.getProductRole());
            assertNotNull(userInfo.getId());
        });
        verify(partyConnectorMock, times(1))
                .getRelationships(institution.getId());
        verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock, userRegistryConnectorMock, onboardingValidationStrategyMock, msPartyRegistryProxyConnectorMock);
    }

    @Test
    void oldContactOnboarding_institutionExists_nullBilling() {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);
        OnboardingImportData onboardingImportData = mockInstance(new OnboardingImportData(), "setUsers", "setBilling", "setInstitutionUpdate", "setPricingPlan");
        onboardingImportData.setBilling(new Billing());
        onboardingImportData.setInstitutionUpdate(new InstitutionUpdate());
        onboardingImportData.setUsers(List.of(userInfo1, userInfo2));
        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId", "setId");
        productMock.setId(onboardingImportData.getProductId());
        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRoleInfo.ProductRole productRole1 = mockInstance(new ProductRoleInfo.ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRoleInfo.ProductRole productRole2 = mockInstance(new ProductRoleInfo.ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        InstitutionResource institutionResourceMock = mockInstance(new InstitutionResource(), "setCategory", "setTaxCode");
        institutionResourceMock.setCategory("L6");
        institutionResourceMock.setTaxCode(onboardingImportData.getInstitutionExternalId());
        Relationships relationshipsMock = mockInstance(new Relationships());
        relationshipsMock.setItems(List.of(mockInstance(new Relationship(), "setId", "setBilling")));
        relationshipsMock.getItems().get(0).setId(institution.getId());
        when(partyConnectorMock.getInstitutionByExternalId(anyString()))
                .thenReturn(institution);
        when(msPartyRegistryProxyConnectorMock.findInstitution(anyString()))
                .thenReturn(institutionResourceMock);
        productMock.setRoleMappings(roleMappings);
        when(productsConnectorMock.getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType()))
                .thenReturn(productMock);
        when(userRegistryConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenThrow(InstitutionDoesNotExistException.class);
        when(partyConnectorMock.getRelationships(any()))
                .thenReturn(relationshipsMock);
        // when
        onboardingServiceImpl.oldContractOnboarding(onboardingImportData);
        // then
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingImportData.getInstitutionExternalId(), onboardingImportData.getProductId());
        verify(msPartyRegistryProxyConnectorMock, times(1))
                .findInstitution(onboardingImportData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingImportData.getInstitutionExternalId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingImportData.getProductId(), onboardingImportData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .oldContractOnboardingOrganization(onboardingImportDataCaptor.capture());
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        onboardingImportData.getUsers().forEach(user ->
                verify(userRegistryConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verify(userRegistryConnectorMock, times(2))
                .saveUser(saveUserCaptor.capture());
        List<SaveUserDto> savedUsers = saveUserCaptor.getAllValues();
        savedUsers.forEach(saveUserDto -> assertTrue(saveUserDto.getWorkContacts().containsKey(institution.getId())));
        OnboardingImportData captured = onboardingImportDataCaptor.getValue();
        assertNotNull(captured.getUsers());
        assertEquals(2, captured.getUsers().size());
        captured.getUsers().forEach(userInfo -> {
            assertEquals(productRole, userInfo.getProductRole());
            assertNotNull(userInfo.getId());
        });
        verify(partyConnectorMock, times(1))
                .getRelationships(institution.getId());
        verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock, userRegistryConnectorMock, onboardingValidationStrategyMock, msPartyRegistryProxyConnectorMock);
    }

    @Test
    void oldContractOnboarding_createInstitution_PA() {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);
        OnboardingImportData onboardingImportData = mockInstance(new OnboardingImportData(), "setUsers", "setBilling", "setInstitutionUpdate", "setPricingPlan");
        onboardingImportData.setBilling(new Billing());
        onboardingImportData.setInstitutionUpdate(new InstitutionUpdate());
        onboardingImportData.setInstitutionType(InstitutionType.PA);
        onboardingImportData.setUsers(List.of(userInfo1, userInfo2));
        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId", "setId");
        productMock.setId(onboardingImportData.getProductId());
        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRoleInfo.ProductRole productRole1 = mockInstance(new ProductRoleInfo.ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRoleInfo.ProductRole productRole2 = mockInstance(new ProductRoleInfo.ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        InstitutionResource institutionResourceMock = mockInstance(new InstitutionResource(), "setCategory");
        institutionResourceMock.setCategory("L6");
        when(partyConnectorMock.getInstitutionByExternalId(anyString()))
                .thenThrow(ResourceNotFoundException.class);
        when(partyConnectorMock.createInstitutionUsingExternalId(anyString()))
                .thenReturn(institution);
        when(msPartyRegistryProxyConnectorMock.findInstitution(anyString()))
                .thenReturn(institutionResourceMock);
        productMock.setRoleMappings(roleMappings);
        when(productsConnectorMock.getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType()))
                .thenReturn(productMock);
        when(userRegistryConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenThrow(InstitutionDoesNotExistException.class);
        // when
        onboardingServiceImpl.oldContractOnboarding(onboardingImportData);
        // then
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingImportData.getInstitutionExternalId(), onboardingImportData.getProductId());
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingImportData.getInstitutionExternalId());
        verify(msPartyRegistryProxyConnectorMock, times(1))
                .findInstitution(onboardingImportData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .createInstitutionUsingExternalId(onboardingImportData.getInstitutionExternalId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingImportData.getProductId(), onboardingImportData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .oldContractOnboardingOrganization(onboardingImportDataCaptor.capture());
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        onboardingImportData.getUsers().forEach(user ->
                verify(userRegistryConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verify(userRegistryConnectorMock, times(2))
                .saveUser(saveUserCaptor.capture());
        List<SaveUserDto> savedUsers = saveUserCaptor.getAllValues();
        savedUsers.forEach(saveUserDto -> assertTrue(saveUserDto.getWorkContacts().containsKey(institution.getId())));
        OnboardingImportData captured = onboardingImportDataCaptor.getValue();
        assertNotNull(captured.getUsers());
        assertEquals(2, captured.getUsers().size());
        captured.getUsers().forEach(userInfo -> {
            assertEquals(productRole, userInfo.getProductRole());
            assertNotNull(userInfo.getId());
        });
        verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock, userRegistryConnectorMock, onboardingValidationStrategyMock, msPartyRegistryProxyConnectorMock);
    }

    @Test
    void oldContractOnboarding_userDataNotMutable() {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);
        OnboardingImportData onboardingImportData = mockInstance(new OnboardingImportData(), "setUsers", "setBilling", "setInstitutionUpdate", "setPricingPlan");
        onboardingImportData.setBilling(new Billing());
        onboardingImportData.setInstitutionUpdate(new InstitutionUpdate());
        onboardingImportData.setInstitutionType(InstitutionType.PA);
        onboardingImportData.setUsers(List.of(userInfo1, userInfo2));
        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId", "setId");
        productMock.setId(onboardingImportData.getProductId());
        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRoleInfo.ProductRole productRole1 = mockInstance(new ProductRoleInfo.ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRoleInfo.ProductRole productRole2 = mockInstance(new ProductRoleInfo.ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        InstitutionResource institutionResourceMock = mockInstance(new InstitutionResource(), "setCategory");
        institutionResourceMock.setCategory("L6");
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenThrow(InstitutionDoesNotExistException.class);
        when(partyConnectorMock.getInstitutionByExternalId(anyString()))
                .thenThrow(ResourceNotFoundException.class);
        when(msPartyRegistryProxyConnectorMock.findInstitution(anyString()))
                .thenReturn(institutionResourceMock);
        when(partyConnectorMock.createInstitutionUsingExternalId(anyString()))
                .thenReturn(institution);
        productMock.setRoleMappings(roleMappings);
        when(productsConnectorMock.getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType()))
                .thenReturn(productMock);

        when(userRegistryConnectorMock.search(any(), any()))
                .thenAnswer(invocation -> {
                    final String taxCode = invocation.getArgument(0, String.class);
                    if (userInfo1.getTaxCode().equals(taxCode)) {
                        return Optional.empty();
                    } else {
                        final it.pagopa.selfcare.external_api.model.user.User user = new it.pagopa.selfcare.external_api.model.user.User();
                        final CertifiedField<String> familyName = new CertifiedField<>();
                        familyName.setCertification(Certification.NONE);
                        familyName.setValue("setSurname2");
                        user.setFamilyName(familyName);
                        final CertifiedField<String> email = new CertifiedField<>();
                        email.setCertification(Certification.SPID);
                        email.setValue("different value");
                        final WorkContact workContact = new WorkContact();
                        workContact.setEmail(email);
                        user.setWorkContacts(Map.of(institution.getId(), workContact));
                        return Optional.of(user);
                    }
                });
        when(userRegistryConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        // when
        final Executable executable = () -> onboardingServiceImpl.oldContractOnboarding(onboardingImportData);
        // then
        assertThrows(UpdateNotAllowedException.class, executable);
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingImportData.getInstitutionExternalId(), onboardingImportData.getProductId());
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingImportData.getInstitutionExternalId());
        verify(msPartyRegistryProxyConnectorMock, times(1))
                .findInstitution(onboardingImportData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .createInstitutionUsingExternalId(onboardingImportData.getInstitutionExternalId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingImportData.getProductId(), onboardingImportData.getInstitutionExternalId());
        verify(userRegistryConnectorMock, times(1))
                .saveUser(any());
        onboardingImportData.getUsers().forEach(user ->
                verify(userRegistryConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock, userRegistryConnectorMock, onboardingValidationStrategyMock, msPartyRegistryProxyConnectorMock);
    }

    @Test
    void oldContractOnboarding_userDataMutable() {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);

        OnboardingImportData onboardingImportData = mockInstance(new OnboardingImportData(), "setUsers", "setBilling", "setInstitutionUpdate", "setPricingPlan");
        onboardingImportData.setBilling(new Billing());
        onboardingImportData.setInstitutionUpdate(new InstitutionUpdate());
        onboardingImportData.setInstitutionType(InstitutionType.PA);
        onboardingImportData.setUsers(List.of(userInfo1, userInfo2));

        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId", "setId");
        productMock.setId(onboardingImportData.getProductId());
        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRoleInfo.ProductRole productRole1 = mockInstance(new ProductRoleInfo.ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRoleInfo.ProductRole productRole2 = mockInstance(new ProductRoleInfo.ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        productMock.setRoleMappings(roleMappings);
        InstitutionResource institutionResourceMock = mockInstance(new InstitutionResource(), "setCategory");
        institutionResourceMock.setCategory("L6");
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenThrow(InstitutionDoesNotExistException.class);
        when(productsConnectorMock.getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType()))
                .thenReturn(productMock);
        when(msPartyRegistryProxyConnectorMock.findInstitution(anyString()))
                .thenReturn(institutionResourceMock);
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        when(partyConnectorMock.getInstitutionByExternalId(anyString()))
                .thenThrow(ResourceNotFoundException.class);
        when(partyConnectorMock.createInstitutionUsingExternalId(anyString()))
                .thenReturn(institution);

        when(userRegistryConnectorMock.search(any(), any()))
                .thenAnswer(invocation -> {
                    final String taxCode = invocation.getArgument(0, String.class);
                    if (userInfo1.getTaxCode().equals(taxCode)) {
                        return Optional.empty();
                    } else {
                        final it.pagopa.selfcare.external_api.model.user.User user = new it.pagopa.selfcare.external_api.model.user.User();
                        final CertifiedField<String> name = new CertifiedField<>();
                        name.setCertification(Certification.NONE);
                        name.setValue("setName2");
                        user.setName(name);
                        final CertifiedField<String> familyName = new CertifiedField<>();
                        familyName.setCertification(Certification.NONE);
                        familyName.setValue("setSurname1");
                        user.setFamilyName(familyName);
                        final CertifiedField<String> email = new CertifiedField<>();
                        email.setCertification(Certification.NONE);
                        email.setValue("setEmail1");
                        final WorkContact workContact = new WorkContact();
                        workContact.setEmail(email);
                        user.setWorkContacts(Map.of(institution.getId(), workContact));
                        user.setId(UUID.randomUUID().toString());
                        return Optional.of(user);
                    }
                });
        when(userRegistryConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        //when
        final Executable executable = () -> onboardingServiceImpl.oldContractOnboarding(onboardingImportData);
        //then
        assertDoesNotThrow(executable);
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingImportData.getInstitutionExternalId(), onboardingImportData.getProductId());
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingImportData.getInstitutionExternalId());
        verify(msPartyRegistryProxyConnectorMock, times(1))
                .findInstitution(onboardingImportData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .createInstitutionUsingExternalId(onboardingImportData.getInstitutionExternalId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingImportData.getProductId(), onboardingImportData.getInstitutionExternalId());
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        verify(userRegistryConnectorMock, times(1))
                .saveUser(saveUserCaptor.capture());
        onboardingImportData.getUsers().forEach(user ->
                verify(userRegistryConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verify(userRegistryConnectorMock, times(1))
                .updateUser(any(), any());
        verify(partyConnectorMock, times(1))
                .oldContractOnboardingOrganization(any());
        verifyNoMoreInteractions(productsConnectorMock, onboardingValidationStrategyMock, msPartyRegistryProxyConnectorMock);
    }

    @Test
    void oldContractOnboarding_userDataMutable1() {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);

        OnboardingImportData onboardingImportData = mockInstance(new OnboardingImportData(), "setUsers", "setBilling", "setInstitutionUpdate", "setPricingPlan");
        onboardingImportData.setBilling(new Billing());
        onboardingImportData.setInstitutionUpdate(new InstitutionUpdate());
        onboardingImportData.setInstitutionType(InstitutionType.PA);
        onboardingImportData.setUsers(List.of(userInfo1, userInfo2));

        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId", "setId");
        productMock.setId(onboardingImportData.getProductId());
        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRoleInfo.ProductRole productRole1 = mockInstance(new ProductRoleInfo.ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRoleInfo.ProductRole productRole2 = mockInstance(new ProductRoleInfo.ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        productMock.setRoleMappings(roleMappings);
        InstitutionResource institutionResourceMock = mockInstance(new InstitutionResource(), "setCategory");
        institutionResourceMock.setCategory("L6");
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenThrow(InstitutionDoesNotExistException.class);
        when(productsConnectorMock.getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType()))
                .thenReturn(productMock);
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        when(partyConnectorMock.getInstitutionByExternalId(anyString()))
                .thenThrow(ResourceNotFoundException.class);
        when(msPartyRegistryProxyConnectorMock.findInstitution(anyString()))
                .thenReturn(institutionResourceMock);
        when(partyConnectorMock.createInstitutionUsingExternalId(anyString()))
                .thenReturn(institution);

        when(userRegistryConnectorMock.search(any(), any()))
                .thenAnswer(invocation -> {
                    final String taxCode = invocation.getArgument(0, String.class);
                    if (userInfo1.getTaxCode().equals(taxCode)) {
                        return Optional.empty();
                    } else {
                        final it.pagopa.selfcare.external_api.model.user.User user = new it.pagopa.selfcare.external_api.model.user.User();
                        final CertifiedField<String> name = new CertifiedField<>();
                        name.setCertification(Certification.NONE);
                        name.setValue("setName2");
                        user.setName(name);
                        final CertifiedField<String> familyName = new CertifiedField<>();
                        familyName.setCertification(Certification.SPID);
                        familyName.setValue(userInfo2.getSurname().toUpperCase());
                        user.setFamilyName(familyName);
                        user.setId(UUID.randomUUID().toString());
                        return Optional.of(user);
                    }
                });
        when(userRegistryConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        //when
        final Executable executable = () -> onboardingServiceImpl.oldContractOnboarding(onboardingImportData);
        //then
        assertDoesNotThrow(executable);
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingImportData.getInstitutionExternalId(), onboardingImportData.getProductId());
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingImportData.getInstitutionExternalId());
        verify(msPartyRegistryProxyConnectorMock, times(1))
                .findInstitution(onboardingImportData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .createInstitutionUsingExternalId(onboardingImportData.getInstitutionExternalId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingImportData.getProductId(), onboardingImportData.getInstitutionExternalId());
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        verify(userRegistryConnectorMock, times(1))
                .saveUser(saveUserCaptor.capture());
        onboardingImportData.getUsers().forEach(user ->
                verify(userRegistryConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verify(userRegistryConnectorMock, times(1))
                .updateUser(any(), any());
        verify(partyConnectorMock, times(1))
                .oldContractOnboardingOrganization(any());
        verifyNoMoreInteractions(productsConnectorMock, onboardingValidationStrategyMock, msPartyRegistryProxyConnectorMock);
    }

    @Test
    void oldContractOnboarding_userDataMutable3() {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);

        OnboardingImportData onboardingImportData = mockInstance(new OnboardingImportData(), "setUsers", "setBilling", "setInstitutionUpdate", "setPricingPlan");
        onboardingImportData.setBilling(new Billing());
        onboardingImportData.setInstitutionUpdate(new InstitutionUpdate());
        onboardingImportData.setInstitutionType(InstitutionType.PA);
        onboardingImportData.setUsers(List.of(userInfo1, userInfo2));

        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId", "setId");
        productMock.setId(onboardingImportData.getProductId());
        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRoleInfo.ProductRole productRole1 = mockInstance(new ProductRoleInfo.ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRoleInfo.ProductRole productRole2 = mockInstance(new ProductRoleInfo.ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        productMock.setRoleMappings(roleMappings);
        InstitutionResource institutionResourceMock = mockInstance(new InstitutionResource(), "setCategory");
        institutionResourceMock.setCategory("L6");
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenThrow(InstitutionDoesNotExistException.class);
        when(productsConnectorMock.getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType()))
                .thenReturn(productMock);
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        when(partyConnectorMock.getInstitutionByExternalId(anyString()))
                .thenThrow(ResourceNotFoundException.class);
        when(msPartyRegistryProxyConnectorMock.findInstitution(anyString()))
                .thenReturn(institutionResourceMock);
        when(partyConnectorMock.createInstitutionUsingExternalId(anyString()))
                .thenReturn(institution);

        when(userRegistryConnectorMock.search(any(), any()))
                .thenAnswer(invocation -> {
                    final String taxCode = invocation.getArgument(0, String.class);
                    if (userInfo1.getTaxCode().equals(taxCode)) {
                        return Optional.empty();
                    } else {
                        final it.pagopa.selfcare.external_api.model.user.User user = new it.pagopa.selfcare.external_api.model.user.User();
                        final CertifiedField<String> name = new CertifiedField<>();
                        name.setCertification(Certification.NONE);
                        name.setValue(userInfo2.getName());
                        user.setName(name);
                        final CertifiedField<String> familyName = new CertifiedField<>();
                        familyName.setCertification(Certification.SPID);
                        familyName.setValue("setSurname2");
                        user.setFamilyName(familyName);
                        final CertifiedField<String> email = new CertifiedField<>();
                        email.setCertification(Certification.NONE);
                        email.setValue("setEmail2");
                        final WorkContact workContact = new WorkContact();
                        workContact.setEmail(email);
                        user.setWorkContacts(Map.of(institution.getId(), workContact));
                        user.setId(UUID.randomUUID().toString());
                        user.setId(UUID.randomUUID().toString());
                        return Optional.of(user);
                    }
                });
        when(userRegistryConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        //when
        final Executable executable = () -> onboardingServiceImpl.oldContractOnboarding(onboardingImportData);
        //then
        assertDoesNotThrow(executable);
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingImportData.getInstitutionExternalId(), onboardingImportData.getProductId());
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingImportData.getInstitutionExternalId());
        verify(msPartyRegistryProxyConnectorMock, times(1))
                .findInstitution(onboardingImportData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .createInstitutionUsingExternalId(onboardingImportData.getInstitutionExternalId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingImportData.getProductId(), onboardingImportData.getInstitutionExternalId());
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        verify(userRegistryConnectorMock, times(1))
                .saveUser(saveUserCaptor.capture());
        onboardingImportData.getUsers().forEach(user ->
                verify(userRegistryConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verify(partyConnectorMock, times(1))
                .oldContractOnboardingOrganization(any());
        verifyNoMoreInteractions(productsConnectorMock, onboardingValidationStrategyMock, msPartyRegistryProxyConnectorMock);
    }

    @Test
    void oldContractOnboarding_userDataMutable2() {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);

        OnboardingImportData onboardingImportData = mockInstance(new OnboardingImportData(), "setUsers", "setBilling", "setInstitutionUpdate", "setPricingPlan");
        onboardingImportData.setBilling(new Billing());
        onboardingImportData.setInstitutionUpdate(new InstitutionUpdate());
        onboardingImportData.setInstitutionType(InstitutionType.PA);
        onboardingImportData.setUsers(List.of(userInfo1, userInfo2));

        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId", "setId");
        productMock.setId(onboardingImportData.getProductId());
        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRoleInfo.ProductRole productRole1 = mockInstance(new ProductRoleInfo.ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRoleInfo.ProductRole productRole2 = mockInstance(new ProductRoleInfo.ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        productMock.setRoleMappings(roleMappings);
        InstitutionResource institutionResourceMock = mockInstance(new InstitutionResource(), "setCategory");
        institutionResourceMock.setCategory("L6");
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenThrow(InstitutionDoesNotExistException.class);
        when(productsConnectorMock.getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType()))
                .thenReturn(productMock);
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        when(partyConnectorMock.getInstitutionByExternalId(anyString()))
                .thenThrow(ResourceNotFoundException.class);
        when(msPartyRegistryProxyConnectorMock.findInstitution(anyString()))
                .thenReturn(institutionResourceMock);
        when(partyConnectorMock.createInstitutionUsingExternalId(anyString()))
                .thenReturn(institution);

        when(userRegistryConnectorMock.search(any(), any()))
                .thenAnswer(invocation -> {
                    final String taxCode = invocation.getArgument(0, String.class);
                    if (userInfo1.getTaxCode().equals(taxCode)) {
                        return Optional.empty();
                    } else {
                        final it.pagopa.selfcare.external_api.model.user.User user = new it.pagopa.selfcare.external_api.model.user.User();
                        final CertifiedField<String> name = new CertifiedField<>();
                        name.setCertification(Certification.NONE);
                        name.setValue("setName3");
                        user.setName(name);
                        final CertifiedField<String> familyName = new CertifiedField<>();
                        familyName.setCertification(Certification.SPID);
                        familyName.setValue("setSurname2");
                        user.setFamilyName(familyName);
                        final CertifiedField<String> email = new CertifiedField<>();
                        email.setCertification(Certification.NONE);
                        email.setValue("setEmail1");
                        final WorkContact workContact = new WorkContact();
                        workContact.setEmail(email);
                        user.setWorkContacts(Map.of("differentKey", workContact));
                        user.setId(UUID.randomUUID().toString());
                        return Optional.of(user);
                    }
                });
        when(userRegistryConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        //when
        final Executable executable = () -> onboardingServiceImpl.oldContractOnboarding(onboardingImportData);
        //then
        assertDoesNotThrow(executable);
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingImportData.getInstitutionExternalId(), onboardingImportData.getProductId());
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingImportData.getInstitutionExternalId());
        verify(msPartyRegistryProxyConnectorMock, times(1))
                .findInstitution(onboardingImportData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .createInstitutionUsingExternalId(onboardingImportData.getInstitutionExternalId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingImportData.getProductId(), onboardingImportData.getInstitutionExternalId());
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        verify(userRegistryConnectorMock, times(1))
                .saveUser(saveUserCaptor.capture());
        onboardingImportData.getUsers().forEach(user ->
                verify(userRegistryConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verify(userRegistryConnectorMock, times(1))
                .updateUser(any(), any());
        verify(partyConnectorMock, times(1))
                .oldContractOnboardingOrganization(any());
        verifyNoMoreInteractions(productsConnectorMock, onboardingValidationStrategyMock, msPartyRegistryProxyConnectorMock);
    }

    @Test
    void oldContractOnboarding_createInstitution_GSP_withCategoryL37() {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);
        OnboardingImportData onboardingImportData = mockInstance(new OnboardingImportData(), "setUsers", "setBilling", "setInstitutionUpdate", "setPricingPlan");
        onboardingImportData.setBilling(new Billing());
        onboardingImportData.setInstitutionUpdate(new InstitutionUpdate());
        onboardingImportData.setInstitutionType(InstitutionType.GSP);
        onboardingImportData.setUsers(List.of(userInfo1, userInfo2));
        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId", "setId");
        productMock.setId(onboardingImportData.getProductId());
        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRoleInfo.ProductRole productRole1 = mockInstance(new ProductRoleInfo.ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRoleInfo.ProductRole productRole2 = mockInstance(new ProductRoleInfo.ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        InstitutionResource institutionResourceMock = mockInstance(new InstitutionResource(), "setCategory");
        institutionResourceMock.setCategory("L37");
        when(partyConnectorMock.getInstitutionByExternalId(anyString()))
                .thenThrow(ResourceNotFoundException.class);
        when(partyConnectorMock.createInstitutionUsingExternalId(anyString()))
                .thenReturn(institution);
        when(msPartyRegistryProxyConnectorMock.findInstitution(anyString()))
                .thenReturn(institutionResourceMock);
        productMock.setRoleMappings(roleMappings);
        when(productsConnectorMock.getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType()))
                .thenReturn(productMock);
        when(userRegistryConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenThrow(InstitutionDoesNotExistException.class);
        // when
        onboardingServiceImpl.oldContractOnboarding(onboardingImportData);
        // then
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingImportData.getInstitutionExternalId(), onboardingImportData.getProductId());
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingImportData.getInstitutionExternalId());
        verify(msPartyRegistryProxyConnectorMock, times(1))
                .findInstitution(onboardingImportData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .createInstitutionUsingExternalId(onboardingImportData.getInstitutionExternalId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingImportData.getProductId(), onboardingImportData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .oldContractOnboardingOrganization(onboardingImportDataCaptor.capture());
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        onboardingImportData.getUsers().forEach(user ->
                verify(userRegistryConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verify(userRegistryConnectorMock, times(2))
                .saveUser(saveUserCaptor.capture());
        List<SaveUserDto> savedUsers = saveUserCaptor.getAllValues();
        savedUsers.forEach(saveUserDto -> assertTrue(saveUserDto.getWorkContacts().containsKey(institution.getId())));
        OnboardingImportData captured = onboardingImportDataCaptor.getValue();
        assertEquals(InstitutionType.GSP, onboardingImportData.getInstitutionType());
        assertNotNull(captured.getUsers());
        assertEquals(2, captured.getUsers().size());
        captured.getUsers().forEach(userInfo -> {
            assertEquals(productRole, userInfo.getProductRole());
            assertNotNull(userInfo.getId());
        });
        verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock, userRegistryConnectorMock, onboardingValidationStrategyMock, msPartyRegistryProxyConnectorMock);
    }

    @Test
    void oldContractOnboarding_nullInstitutionType() {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);
        OnboardingImportData onboardingImportData = mockInstance(new OnboardingImportData(), "setUsers", "setBilling", "setInstitutionUpdate", "setPricingPlan");
        onboardingImportData.setBilling(new Billing());
        onboardingImportData.setInstitutionUpdate(new InstitutionUpdate());
        onboardingImportData.setUsers(List.of(userInfo1, userInfo2));
        Product baseProductMock = mockInstance(new Product(), "setRoleMappings", "setParentId", "setId");
        baseProductMock.setId(onboardingImportData.getProductId());
        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRoleInfo.ProductRole productRole1 = mockInstance(new ProductRoleInfo.ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRoleInfo.ProductRole productRole2 = mockInstance(new ProductRoleInfo.ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        baseProductMock.setRoleMappings(roleMappings);
        Institution institution = mockInstance(new Institution(), "setInstitutionType");
        institution.setId(UUID.randomUUID().toString());
        UserInfo managerInfo = mockInstance(new UserInfo());
        managerInfo.setInstitutionId(institution.getId());
        InstitutionResource institutionResourceMock = mockInstance(new InstitutionResource(), "setCategory");
        institutionResourceMock.setCategory("L6");
        Relationships relationshipsMock = mockInstance(new Relationships());
        relationshipsMock.setItems(List.of(mockInstance(new Relationship(), "setId")));
        relationshipsMock.getItems().get(0).setId(institution.getId());
        when(partyConnectorMock.verifyOnboarding(onboardingImportData.getInstitutionExternalId(), baseProductMock.getId()))
                .thenThrow(InstitutionDoesNotExistException.class);
        when(partyConnectorMock.getInstitutionByExternalId(anyString()))
                .thenReturn(institution);
        when(msPartyRegistryProxyConnectorMock.findInstitution(anyString()))
                .thenReturn(institutionResourceMock);
        when(productsConnectorMock.getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType()))
                .thenReturn(baseProductMock);
        when(userRegistryConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        when(partyConnectorMock.getRelationships(any()))
                .thenReturn(relationshipsMock);
        // when
        onboardingServiceImpl.oldContractOnboarding(onboardingImportData);
        // then
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingImportData.getInstitutionExternalId(), baseProductMock.getId());
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingImportData.getInstitutionExternalId());
        verify(msPartyRegistryProxyConnectorMock, times(1))
                .findInstitution(onboardingImportData.getInstitutionExternalId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType());
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingImportData.getInstitutionExternalId(), baseProductMock.getId());
        verify(partyConnectorMock, times(1))
                .oldContractOnboardingOrganization(onboardingImportDataCaptor.capture());
        OnboardingImportData captured = onboardingImportDataCaptor.getValue();
        System.out.println(captured);
        assertNotNull(captured.getUsers());
        assertEquals(onboardingImportData.getUsers().size(), captured.getUsers().size());
        assertEquals(InstitutionType.PA, captured.getInstitutionType());
        captured.getUsers().forEach(userInfo -> {
            assertEquals(productRole, userInfo.getProductRole());
            assertNotNull(userInfo.getId());
            checkNotNullFields(userInfo);
        });
        onboardingImportData.getUsers().forEach(user ->
                verify(userRegistryConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        verify(userRegistryConnectorMock, times(onboardingImportData.getUsers().size()))
                .saveUser(saveUserCaptor.capture());
        List<SaveUserDto> savedUsers = saveUserCaptor.getAllValues();
        savedUsers.forEach(saveUserDto -> assertTrue(saveUserDto.getWorkContacts().containsKey(institution.getId())));
        verify(onboardingValidationStrategyMock, times(1))
                .validate(baseProductMock.getId(), onboardingImportData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .getRelationships(institution.getId());
        verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock, userRegistryConnectorMock, onboardingValidationStrategyMock, msPartyRegistryProxyConnectorMock);
    }

    @Test
    void autoApprovalOnboarding_institutionAlreadyOnboardedException() {
        // given
        OnboardingData onboardingData = mockInstance(new OnboardingData());
        ResponseEntity<Void> responseEntityMock = new ResponseEntity<>(HttpStatus.NO_CONTENT);
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenReturn(responseEntityMock);
        // when
        Executable executable = () -> onboardingServiceImpl.autoApprovalOnboarding(onboardingData);
        // then
        InstitutionAlreadyOnboardedException e = assertThrows(InstitutionAlreadyOnboardedException.class, executable);
        assertEquals(String.format("The institution with external id %s is already onboarded on the product %s",
                        onboardingData.getInstitutionExternalId(),
                        onboardingData.getProductId()),
                e.getMessage());
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingData.getInstitutionExternalId(), onboardingData.getProductId());
        verifyNoMoreInteractions(partyConnectorMock);
        verifyNoInteractions(userRegistryConnectorMock, productsConnectorMock);
    }

    @Test
    void autoApprovalOnboarding_noExceptionsRaised() {
        // given
        OnboardingData onboardingData = mockInstance(new OnboardingData());
        ResponseEntity<Void> responseEntityMock = new ResponseEntity<>(HttpStatus.OK);
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenReturn(responseEntityMock);
        // when
        Executable executable = () -> onboardingServiceImpl.autoApprovalOnboarding(onboardingData);
        // then
        assertDoesNotThrow(executable);
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingData.getInstitutionExternalId(), onboardingData.getProductId());
        verifyNoMoreInteractions(partyConnectorMock);
        verifyNoInteractions(userRegistryConnectorMock, productsConnectorMock);
    }

    @Test
    void autoApprovalOnboarding_nullOnboardingData() {
        // given
        OnboardingData onboardingData = null;
        // when
        Executable executable = () -> onboardingServiceImpl.autoApprovalOnboarding(onboardingData);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_ONBOARDING_DATA_MESSAGE, e.getMessage());
        verifyNoInteractions(partyConnectorMock, productsConnectorMock, userRegistryConnectorMock, onboardingValidationStrategyMock);
    }


    @Test
    void autoApprovalOnboarding_nullRoleMapping() {
        // given
        OnboardingData onboardingData = mockInstance(new OnboardingData());
        Product product = mockInstance(new Product(), "setId", "setParentId");
        product.setId(onboardingData.getProductId());
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenThrow(InstitutionDoesNotExistException.class);
        when(productsConnectorMock.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType()))
                .thenReturn(product);
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        // when
        Executable executable = () -> onboardingServiceImpl.autoApprovalOnboarding(onboardingData);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("Role mappings is required", e.getMessage());
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingData.getInstitutionExternalId(), onboardingData.getProductId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingData.getProductId(), onboardingData.getInstitutionExternalId());
        verifyNoMoreInteractions(productsConnectorMock, onboardingValidationStrategyMock);
        verifyNoInteractions(userRegistryConnectorMock);
    }

    @Test
    void autoApprovalOnboarding_subProductPhaseOutException() {
        // given
        OnboardingData onboardingData = mockInstance(new OnboardingData());
        Product product = mockInstance(new Product(), "setId", "setParentId");
        product.setId(onboardingData.getProductId());
        product.setStatus(ProductStatus.PHASE_OUT);
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenThrow(ResourceNotFoundException.class);
        when(productsConnectorMock.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType()))
                .thenReturn(product);
        // when
        Executable executable = () -> onboardingServiceImpl.autoApprovalOnboarding(onboardingData);
        // then
        ValidationException e = assertThrows(ValidationException.class, executable);
        assertEquals(String.format("Unable to complete the onboarding for institution with external id '%s' to product '%s', the product is dismissed.",
                        onboardingData.getInstitutionExternalId(),
                        product.getId()),
                e.getMessage());
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingData.getInstitutionExternalId(), onboardingData.getProductId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        verifyNoMoreInteractions(productsConnectorMock);
        verifyNoInteractions(userRegistryConnectorMock);
    }


    @Test
    void autoApprovalOnboarding_baseProductPhaseOutException() {
        // given
        OnboardingData onboardingData = mockInstance(new OnboardingData());
        Product product = mockInstance(new Product(), "setId", "setParentId");
        Product product2 = mockInstance(new Product(), "setId", "setParentId");
        String parentId = "parentId";
        product2.setId(parentId);
        product2.setStatus(ProductStatus.PHASE_OUT);
        product.setId(onboardingData.getProductId());
        product.setStatus(ProductStatus.ACTIVE);
        product.setParentId(parentId);
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenThrow(InstitutionDoesNotExistException.class);
        when(productsConnectorMock.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType()))
                .thenReturn(product);
        when(productsConnectorMock.getProduct(product.getParentId(), null))
                .thenReturn(product2);
        // when
        Executable executable = () -> onboardingServiceImpl.autoApprovalOnboarding(onboardingData);
        // then
        ValidationException e = assertThrows(ValidationException.class, executable);
        assertEquals(String.format("Unable to complete the onboarding for institution with external id '%s' to product '%s', the base product is dismissed.",
                        onboardingData.getInstitutionExternalId(),
                        product.getParentId()),
                e.getMessage());
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingData.getInstitutionExternalId(), onboardingData.getProductId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        verify(productsConnectorMock, times(1))
                .getProduct(product.getParentId(), null);
        verifyNoMoreInteractions(productsConnectorMock);
        verifyNoInteractions(userRegistryConnectorMock);
    }

    @Test
    void autoApprovalOnboarding_nullBillingData() {
        //given
        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setBilling");
        //when
        Executable executable = () -> onboardingServiceImpl.autoApprovalOnboarding(onboardingData);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_INSTITUTION_BILLING_DATA_MESSAGE, e.getMessage());
        verifyNoInteractions(productsConnectorMock, partyConnectorMock, userRegistryConnectorMock, onboardingValidationStrategyMock);
    }

    @Test
    void autoApprovalOnboarding_nullOrganizationType() {
        //given
        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setInstitutionType");
        Billing billing = mockInstance(new Billing());
        onboardingData.setBilling(billing);
        //when
        Executable executable = () -> onboardingServiceImpl.autoApprovalOnboarding(onboardingData);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_INSTITUTION_TYPE_MESSAGE, e.getMessage());
        verifyNoInteractions(productsConnectorMock, partyConnectorMock, userRegistryConnectorMock, onboardingValidationStrategyMock);
    }


    @Test
    void autoApprovalOnboarding_notAllowed() {
        // given
        User userInfo = mockInstance(new User(), "setRole");
        userInfo.setRole(PartyRole.MANAGER);
        OnboardingData onboardingData = mockInstance(new OnboardingData());
        Billing billing = mockInstance(new Billing());
        onboardingData.setUsers(List.of(userInfo));
        onboardingData.setBilling(billing);
        Product product = mockInstance(new Product(), "setId", "setParentId");
        product.setId(onboardingData.getProductId());
        product.setRoleMappings(new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, null);
        }});
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenThrow(InstitutionDoesNotExistException.class);
        when(productsConnectorMock.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType()))
                .thenReturn(product);
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(false);
        // when
        Executable executable = () -> onboardingServiceImpl.autoApprovalOnboarding(onboardingData);
        // then
        Exception e = assertThrows(OnboardingNotAllowedException.class, executable);
        assertEquals("Institution with external id '" + onboardingData.getInstitutionExternalId() + "' is not allowed to onboard '" + onboardingData.getProductId() + "' product",
                e.getMessage());
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingData.getInstitutionExternalId(), onboardingData.getProductId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingData.getProductId(), onboardingData.getInstitutionExternalId());
        verifyNoMoreInteractions(productsConnectorMock, onboardingValidationStrategyMock);
        verifyNoInteractions(userRegistryConnectorMock);
    }


    @Test
    void autoApprovalOnboarding_nullProductRoles() {
        // given
        User userInfo = mockInstance(new User(), "setRole");
        userInfo.setRole(PartyRole.MANAGER);
        OnboardingData onboardingData = mockInstance(new OnboardingData());
        Billing billing = mockInstance(new Billing());
        onboardingData.setUsers(List.of(userInfo));
        onboardingData.setBilling(billing);
        Product product = mockInstance(new Product(), "setParentId", "setId");
        product.setId(onboardingData.getProductId());
        product.setRoleMappings(new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, null);
        }});
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenThrow(InstitutionDoesNotExistException.class);
        when(productsConnectorMock.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType()))
                .thenReturn(product);
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        // when
        Executable executable = () -> onboardingServiceImpl.autoApprovalOnboarding(onboardingData);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(String.format(ATLEAST_ONE_PRODUCT_ROLE_REQUIRED, userInfo.getRole()), e.getMessage());
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingData.getInstitutionExternalId(), onboardingData.getProductId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingData.getProductId(), onboardingData.getInstitutionExternalId());
        verifyNoMoreInteractions(productsConnectorMock, onboardingValidationStrategyMock);
        verifyNoInteractions(userRegistryConnectorMock);
    }


    @Test
    void autoApprovalOnboarding_emptyProductRoles() {
        // given
        User userInfo = mockInstance(new User(), "setRole");
        userInfo.setRole(PartyRole.DELEGATE);
        OnboardingData onboardingData = mockInstance(new OnboardingData());
        Billing billing = mockInstance(new Billing());
        onboardingData.setBilling(billing);
        onboardingData.setUsers(List.of(userInfo));
        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId", "setId");
        productMock.setId(onboardingData.getProductId());
        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        productRoleInfo1.setRoles(List.of(mockInstance(new ProductRoleInfo.ProductRole(), 1)));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        productRoleInfo2.setRoles(List.of());
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenThrow(InstitutionDoesNotExistException.class);
        productMock.setRoleMappings(roleMappings);
        when(productsConnectorMock.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType()))
                .thenReturn(productMock);
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        // when
        Executable executable = () -> onboardingServiceImpl.autoApprovalOnboarding(onboardingData);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(String.format(ATLEAST_ONE_PRODUCT_ROLE_REQUIRED, userInfo.getRole()), e.getMessage());
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingData.getInstitutionExternalId(), onboardingData.getProductId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingData.getProductId(), onboardingData.getInstitutionExternalId());
        verifyNoMoreInteractions(productsConnectorMock, onboardingValidationStrategyMock);
        verifyNoInteractions(userRegistryConnectorMock);
    }

    @Test
    void autoApprovalOnboarding_MoreThanOneProductRoles() {
        // given
        User userInfo = mockInstance(new User(), "setRole");
        userInfo.setRole(PartyRole.DELEGATE);
        OnboardingData onboardingData = mockInstance(new OnboardingData());
        Billing billing = mockInstance(new Billing());
        onboardingData.setBilling(billing);
        onboardingData.setUsers(List.of(userInfo));
        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId", "setId");
        productMock.setId(onboardingData.getProductId());
        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        productRoleInfo1.setRoles(List.of(mockInstance(new ProductRoleInfo.ProductRole(), 1)));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        productRoleInfo2.setRoles(List.of(mockInstance(new ProductRoleInfo.ProductRole(), 1),
                mockInstance(new ProductRoleInfo.ProductRole(), 2)));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        productMock.setRoleMappings(roleMappings);
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenThrow(InstitutionDoesNotExistException.class);
        when(productsConnectorMock.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType()))
                .thenReturn(productMock);
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        // when
        Executable executable = () -> onboardingServiceImpl.autoApprovalOnboarding(onboardingData);
        // then
        IllegalStateException e = assertThrows(IllegalStateException.class, executable);
        assertEquals(String.format(MORE_THAN_ONE_PRODUCT_ROLE_AVAILABLE, userInfo.getRole()), e.getMessage());
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingData.getInstitutionExternalId(), onboardingData.getProductId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingData.getProductId(), onboardingData.getInstitutionExternalId());
        verifyNoMoreInteractions(productsConnectorMock, onboardingValidationStrategyMock);
        verifyNoInteractions(userRegistryConnectorMock);
    }


    @Test
    void autoApprovalOnboarding_institutionExists() {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);
        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setUsers");
        onboardingData.setUsers(List.of(userInfo1, userInfo2));
        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId", "setId");
        productMock.setId(onboardingData.getProductId());
        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRoleInfo.ProductRole productRole1 = mockInstance(new ProductRoleInfo.ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRoleInfo.ProductRole productRole2 = mockInstance(new ProductRoleInfo.ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenThrow(InstitutionDoesNotExistException.class);
        when(partyConnectorMock.getInstitutionByExternalId(anyString()))
                .thenReturn(institution);
        productMock.setRoleMappings(roleMappings);
        when(productsConnectorMock.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType()))
                .thenReturn(productMock);
        when(userRegistryConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        // when
        onboardingServiceImpl.autoApprovalOnboarding(onboardingData);
        // then
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingData.getInstitutionExternalId(), onboardingData.getProductId());
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingData.getInstitutionExternalId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingData.getProductId(), onboardingData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .autoApprovalOnboarding(onboardingDataCaptor.capture());
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        onboardingData.getUsers().forEach(user ->
                verify(userRegistryConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verify(userRegistryConnectorMock, times(2))
                .saveUser(saveUserCaptor.capture());
        List<SaveUserDto> savedUsers = saveUserCaptor.getAllValues();
        savedUsers.forEach(saveUserDto -> assertTrue(saveUserDto.getWorkContacts().containsKey(institution.getId())));
        OnboardingData captured = onboardingDataCaptor.getValue();
        assertNotNull(captured.getUsers());
        assertEquals(2, captured.getUsers().size());
        captured.getUsers().forEach(userInfo -> {
            assertEquals(productRole, userInfo.getProductRole());
            assertNotNull(userInfo.getId());
        });
        verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock, userRegistryConnectorMock, onboardingValidationStrategyMock);
    }

    @Test
    void autoApprovalOnboarding_createInstitution_PA() {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);
        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setInstitutionType", "setUsers");
        onboardingData.setInstitutionType(InstitutionType.PA);
        onboardingData.setUsers(List.of(userInfo1, userInfo2));
        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId", "setId");
        productMock.setId(onboardingData.getProductId());
        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRoleInfo.ProductRole productRole1 = mockInstance(new ProductRoleInfo.ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRoleInfo.ProductRole productRole2 = mockInstance(new ProductRoleInfo.ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenThrow(InstitutionDoesNotExistException.class);
        when(partyConnectorMock.getInstitutionByExternalId(anyString()))
                .thenThrow(ResourceNotFoundException.class);
        when(partyConnectorMock.createInstitutionUsingExternalId(anyString()))
                .thenReturn(institution);
        productMock.setRoleMappings(roleMappings);
        when(productsConnectorMock.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType()))
                .thenReturn(productMock);
        when(userRegistryConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        // when
        onboardingServiceImpl.autoApprovalOnboarding(onboardingData);
        // then
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingData.getInstitutionExternalId(), onboardingData.getProductId());
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .createInstitutionUsingExternalId(onboardingData.getInstitutionExternalId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingData.getProductId(), onboardingData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .autoApprovalOnboarding(onboardingDataCaptor.capture());
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        onboardingData.getUsers().forEach(user ->
                verify(userRegistryConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verify(userRegistryConnectorMock, times(2))
                .saveUser(saveUserCaptor.capture());
        List<SaveUserDto> savedUsers = saveUserCaptor.getAllValues();
        savedUsers.forEach(saveUserDto -> assertTrue(saveUserDto.getWorkContacts().containsKey(institution.getId())));
        OnboardingData captured = onboardingDataCaptor.getValue();
        assertNotNull(captured.getUsers());
        assertEquals(2, captured.getUsers().size());
        captured.getUsers().forEach(userInfo -> {
            assertEquals(productRole, userInfo.getProductRole());
            assertNotNull(userInfo.getId());
        });
        verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock, userRegistryConnectorMock, onboardingValidationStrategyMock);
    }

    @Test
    void autoApprovalOnboarding_createInstitution_ANAC() {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);
        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setInstitutionType", "setUsers");
        onboardingData.setInstitutionType(InstitutionType.SA);
        onboardingData.setUsers(List.of(userInfo1, userInfo2));
        onboardingData.setOrigin("ANAC");
        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId", "setId");
        productMock.setId(onboardingData.getProductId());
        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRoleInfo.ProductRole productRole1 = mockInstance(new ProductRoleInfo.ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRoleInfo.ProductRole productRole2 = mockInstance(new ProductRoleInfo.ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenThrow(InstitutionDoesNotExistException.class);
        when(partyConnectorMock.getInstitutionByExternalId(anyString()))
                .thenThrow(ResourceNotFoundException.class);
        when(partyConnectorMock.createInstitutionFromANAC(onboardingData))
                .thenReturn(institution);
        productMock.setRoleMappings(roleMappings);
        when(productsConnectorMock.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType()))
                .thenReturn(productMock);
        when(userRegistryConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        // when
        onboardingServiceImpl.autoApprovalOnboarding(onboardingData);
        // then
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingData.getInstitutionExternalId(), onboardingData.getProductId());
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .createInstitutionFromANAC(onboardingData);
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingData.getProductId(), onboardingData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .autoApprovalOnboarding(onboardingDataCaptor.capture());
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        onboardingData.getUsers().forEach(user ->
                verify(userRegistryConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verify(userRegistryConnectorMock, times(2))
                .saveUser(saveUserCaptor.capture());
        List<SaveUserDto> savedUsers = saveUserCaptor.getAllValues();
        savedUsers.forEach(saveUserDto -> assertTrue(saveUserDto.getWorkContacts().containsKey(institution.getId())));
        OnboardingData captured = onboardingDataCaptor.getValue();
        assertNotNull(captured.getUsers());
        assertEquals(2, captured.getUsers().size());
        captured.getUsers().forEach(userInfo -> {
            assertEquals(productRole, userInfo.getProductRole());
            assertNotNull(userInfo.getId());
        });
        verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock, userRegistryConnectorMock, onboardingValidationStrategyMock);
    }

    @Test
    void autoApprovalOnboarding_createInstitution_notPA() {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);
        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setInstitutionType", "setUsers");
        onboardingData.setInstitutionType(InstitutionType.PSP);
        onboardingData.setUsers(List.of(userInfo1, userInfo2));
        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId", "setId");
        productMock.setId(onboardingData.getProductId());
        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRoleInfo.ProductRole productRole1 = mockInstance(new ProductRoleInfo.ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRoleInfo.ProductRole productRole2 = mockInstance(new ProductRoleInfo.ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenThrow(InstitutionDoesNotExistException.class);
        when(partyConnectorMock.getInstitutionByExternalId(anyString()))
                .thenThrow(ResourceNotFoundException.class);
        when(partyConnectorMock.createInstitutionRaw(any()))
                .thenReturn(institution);
        productMock.setRoleMappings(roleMappings);
        when(productsConnectorMock.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType()))
                .thenReturn(productMock);
        when(userRegistryConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        // when
        onboardingServiceImpl.autoApprovalOnboarding(onboardingData);
        // then
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingData.getInstitutionExternalId(), onboardingData.getProductId());
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .createInstitutionRaw(onboardingData);
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingData.getProductId(), onboardingData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .autoApprovalOnboarding(onboardingDataCaptor.capture());
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        onboardingData.getUsers().forEach(user ->
                verify(userRegistryConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verify(userRegistryConnectorMock, times(2))
                .saveUser(saveUserCaptor.capture());
        List<SaveUserDto> savedUsers = saveUserCaptor.getAllValues();
        savedUsers.forEach(saveUserDto -> assertTrue(saveUserDto.getWorkContacts().containsKey(institution.getId())));
        OnboardingData captured = onboardingDataCaptor.getValue();
        assertNotNull(captured.getUsers());
        assertEquals(2, captured.getUsers().size());
        captured.getUsers().forEach(userInfo -> {
            assertEquals(productRole, userInfo.getProductRole());
            assertNotNull(userInfo.getId());
        });
        verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock, userRegistryConnectorMock, onboardingValidationStrategyMock);
    }


    @Test
    void autoApprovalOnboarding_userDataNotMutable() {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);
        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setInstitutionType", "setUsers");
        onboardingData.setInstitutionType(InstitutionType.PA);
        onboardingData.setUsers(List.of(userInfo1, userInfo2));
        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId", "setId");
        productMock.setId(onboardingData.getProductId());
        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRoleInfo.ProductRole productRole1 = mockInstance(new ProductRoleInfo.ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRoleInfo.ProductRole productRole2 = mockInstance(new ProductRoleInfo.ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenThrow(InstitutionDoesNotExistException.class);
        when(partyConnectorMock.getInstitutionByExternalId(anyString()))
                .thenThrow(ResourceNotFoundException.class);
        when(partyConnectorMock.createInstitutionUsingExternalId(anyString()))
                .thenReturn(institution);
        productMock.setRoleMappings(roleMappings);
        when(productsConnectorMock.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType()))
                .thenReturn(productMock);

        when(userRegistryConnectorMock.search(any(), any()))
                .thenAnswer(invocation -> {
                    final String taxCode = invocation.getArgument(0, String.class);
                    if (userInfo1.getTaxCode().equals(taxCode)) {
                        return Optional.empty();
                    } else {
                        final it.pagopa.selfcare.external_api.model.user.User user = new it.pagopa.selfcare.external_api.model.user.User();
                        final CertifiedField<String> familyName = new CertifiedField<>();
                        familyName.setCertification(Certification.NONE);
                        familyName.setValue("setSurname2");
                        user.setFamilyName(familyName);
                        final CertifiedField<String> email = new CertifiedField<>();
                        email.setCertification(Certification.SPID);
                        email.setValue("different value");
                        final WorkContact workContact = new WorkContact();
                        workContact.setEmail(email);
                        user.setWorkContacts(Map.of(institution.getId(), workContact));
                        return Optional.of(user);
                    }
                });
        when(userRegistryConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        // when
        final Executable executable = () -> onboardingServiceImpl.autoApprovalOnboarding(onboardingData);
        // then
        assertThrows(UpdateNotAllowedException.class, executable);
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingData.getInstitutionExternalId(), onboardingData.getProductId());
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .createInstitutionUsingExternalId(onboardingData.getInstitutionExternalId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingData.getProductId(), onboardingData.getInstitutionExternalId());
        verify(userRegistryConnectorMock, times(1))
                .saveUser(any());
        onboardingData.getUsers().forEach(user ->
                verify(userRegistryConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock, userRegistryConnectorMock, onboardingValidationStrategyMock);
    }

    @Test
    void autoApprovalOnboarding_userDataMutable() {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);

        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setInstitutionType", "setUsers");
        onboardingData.setInstitutionType(InstitutionType.PA);
        onboardingData.setUsers(List.of(userInfo1, userInfo2));

        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId", "setId");
        productMock.setId(onboardingData.getProductId());
        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRoleInfo.ProductRole productRole1 = mockInstance(new ProductRoleInfo.ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRoleInfo.ProductRole productRole2 = mockInstance(new ProductRoleInfo.ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        productMock.setRoleMappings(roleMappings);
        when(productsConnectorMock.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType()))
                .thenReturn(productMock);
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenThrow(InstitutionDoesNotExistException.class);
        when(partyConnectorMock.getInstitutionByExternalId(anyString()))
                .thenThrow(ResourceNotFoundException.class);
        when(partyConnectorMock.createInstitutionUsingExternalId(anyString()))
                .thenReturn(institution);

        when(userRegistryConnectorMock.search(any(), any()))
                .thenAnswer(invocation -> {
                    final String taxCode = invocation.getArgument(0, String.class);
                    if (userInfo1.getTaxCode().equals(taxCode)) {
                        return Optional.empty();
                    } else {
                        final it.pagopa.selfcare.external_api.model.user.User user = new it.pagopa.selfcare.external_api.model.user.User();
                        final CertifiedField<String> name = new CertifiedField<>();
                        name.setCertification(Certification.NONE);
                        name.setValue("setName2");
                        user.setName(name);
                        final CertifiedField<String> familyName = new CertifiedField<>();
                        familyName.setCertification(Certification.NONE);
                        familyName.setValue("setSurname1");
                        user.setFamilyName(familyName);
                        final CertifiedField<String> email = new CertifiedField<>();
                        email.setCertification(Certification.NONE);
                        email.setValue("setEmail1");
                        final WorkContact workContact = new WorkContact();
                        workContact.setEmail(email);
                        user.setWorkContacts(Map.of(institution.getId(), workContact));
                        user.setId(UUID.randomUUID().toString());
                        return Optional.of(user);
                    }
                });
        when(userRegistryConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        //when
        final Executable executable = () -> onboardingServiceImpl.autoApprovalOnboarding(onboardingData);
        //then
        assertDoesNotThrow(executable);
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingData.getInstitutionExternalId(), onboardingData.getProductId());
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .createInstitutionUsingExternalId(onboardingData.getInstitutionExternalId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingData.getProductId(), onboardingData.getInstitutionExternalId());
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        verify(userRegistryConnectorMock, times(1))
                .saveUser(saveUserCaptor.capture());
        onboardingData.getUsers().forEach(user ->
                verify(userRegistryConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verify(userRegistryConnectorMock, times(1))
                .updateUser(any(), any());
        verify(partyConnectorMock, times(1))
                .autoApprovalOnboarding(any());
        verifyNoMoreInteractions(productsConnectorMock, onboardingValidationStrategyMock);
    }

    @Test
    void autoApprovalOnboarding_userDataMutable1() {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);

        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setInstitutionType", "setUsers");
        onboardingData.setInstitutionType(InstitutionType.PA);
        onboardingData.setUsers(List.of(userInfo1, userInfo2));

        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId", "setId");
        productMock.setId(onboardingData.getProductId());
        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRoleInfo.ProductRole productRole1 = mockInstance(new ProductRoleInfo.ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRoleInfo.ProductRole productRole2 = mockInstance(new ProductRoleInfo.ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        productMock.setRoleMappings(roleMappings);
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenThrow(InstitutionDoesNotExistException.class);
        when(productsConnectorMock.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType()))
                .thenReturn(productMock);
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        when(partyConnectorMock.getInstitutionByExternalId(anyString()))
                .thenThrow(ResourceNotFoundException.class);
        when(partyConnectorMock.createInstitutionUsingExternalId(anyString()))
                .thenReturn(institution);

        when(userRegistryConnectorMock.search(any(), any()))
                .thenAnswer(invocation -> {
                    final String taxCode = invocation.getArgument(0, String.class);
                    if (userInfo1.getTaxCode().equals(taxCode)) {
                        return Optional.empty();
                    } else {
                        final it.pagopa.selfcare.external_api.model.user.User user = new it.pagopa.selfcare.external_api.model.user.User();
                        final CertifiedField<String> name = new CertifiedField<>();
                        name.setCertification(Certification.NONE);
                        name.setValue("setName2");
                        user.setName(name);
                        final CertifiedField<String> familyName = new CertifiedField<>();
                        familyName.setCertification(Certification.SPID);
                        familyName.setValue(userInfo2.getSurname().toUpperCase());
                        user.setFamilyName(familyName);
                        user.setId(UUID.randomUUID().toString());
                        return Optional.of(user);
                    }
                });
        when(userRegistryConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        //when
        final Executable executable = () -> onboardingServiceImpl.autoApprovalOnboarding(onboardingData);
        //then
        assertDoesNotThrow(executable);
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingData.getInstitutionExternalId(), onboardingData.getProductId());
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .createInstitutionUsingExternalId(onboardingData.getInstitutionExternalId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingData.getProductId(), onboardingData.getInstitutionExternalId());
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        verify(userRegistryConnectorMock, times(1))
                .saveUser(saveUserCaptor.capture());
        onboardingData.getUsers().forEach(user ->
                verify(userRegistryConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verify(userRegistryConnectorMock, times(1))
                .updateUser(any(), any());
        verify(partyConnectorMock, times(1))
                .autoApprovalOnboarding(any());
        verifyNoMoreInteractions(productsConnectorMock, onboardingValidationStrategyMock);
    }

    @Test
    void autoApprovalOnboarding_userDataMutable3() {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);

        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setInstitutionType", "setUsers");
        onboardingData.setInstitutionType(InstitutionType.PA);
        onboardingData.setUsers(List.of(userInfo1, userInfo2));

        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId", "setId");
        productMock.setId(onboardingData.getProductId());
        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRoleInfo.ProductRole productRole1 = mockInstance(new ProductRoleInfo.ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRoleInfo.ProductRole productRole2 = mockInstance(new ProductRoleInfo.ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        productMock.setRoleMappings(roleMappings);
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenThrow(InstitutionDoesNotExistException.class);
        when(productsConnectorMock.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType()))
                .thenReturn(productMock);
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        when(partyConnectorMock.getInstitutionByExternalId(anyString()))
                .thenThrow(ResourceNotFoundException.class);
        when(partyConnectorMock.createInstitutionUsingExternalId(anyString()))
                .thenReturn(institution);

        when(userRegistryConnectorMock.search(any(), any()))
                .thenAnswer(invocation -> {
                    final String taxCode = invocation.getArgument(0, String.class);
                    if (userInfo1.getTaxCode().equals(taxCode)) {
                        return Optional.empty();
                    } else {
                        final it.pagopa.selfcare.external_api.model.user.User user = new it.pagopa.selfcare.external_api.model.user.User();
                        final CertifiedField<String> name = new CertifiedField<>();
                        name.setCertification(Certification.NONE);
                        name.setValue(userInfo2.getName());
                        user.setName(name);
                        final CertifiedField<String> familyName = new CertifiedField<>();
                        familyName.setCertification(Certification.SPID);
                        familyName.setValue("setSurname2");
                        user.setFamilyName(familyName);
                        final CertifiedField<String> email = new CertifiedField<>();
                        email.setCertification(Certification.NONE);
                        email.setValue("setEmail2");
                        final WorkContact workContact = new WorkContact();
                        workContact.setEmail(email);
                        user.setWorkContacts(Map.of(institution.getId(), workContact));
                        user.setId(UUID.randomUUID().toString());
                        user.setId(UUID.randomUUID().toString());
                        return Optional.of(user);
                    }
                });
        when(userRegistryConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        //when
        final Executable executable = () -> onboardingServiceImpl.autoApprovalOnboarding(onboardingData);
        //then
        assertDoesNotThrow(executable);
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingData.getInstitutionExternalId(), onboardingData.getProductId());
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .createInstitutionUsingExternalId(onboardingData.getInstitutionExternalId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingData.getProductId(), onboardingData.getInstitutionExternalId());
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        verify(userRegistryConnectorMock, times(1))
                .saveUser(saveUserCaptor.capture());
        onboardingData.getUsers().forEach(user ->
                verify(userRegistryConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verify(partyConnectorMock, times(1))
                .autoApprovalOnboarding(any());
        verifyNoMoreInteractions(productsConnectorMock, onboardingValidationStrategyMock);
    }

    @Test
    void autoApprovalOnboarding_userDataMutable2() {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);

        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setInstitutionType", "setUsers");
        onboardingData.setInstitutionType(InstitutionType.PA);
        onboardingData.setUsers(List.of(userInfo1, userInfo2));

        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId", "setId");
        productMock.setId(onboardingData.getProductId());
        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRoleInfo.ProductRole productRole1 = mockInstance(new ProductRoleInfo.ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRoleInfo.ProductRole productRole2 = mockInstance(new ProductRoleInfo.ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        productMock.setRoleMappings(roleMappings);
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenThrow(InstitutionDoesNotExistException.class);
        when(productsConnectorMock.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType()))
                .thenReturn(productMock);
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        when(partyConnectorMock.getInstitutionByExternalId(anyString()))
                .thenThrow(ResourceNotFoundException.class);
        when(partyConnectorMock.createInstitutionUsingExternalId(anyString()))
                .thenReturn(institution);

        when(userRegistryConnectorMock.search(any(), any()))
                .thenAnswer(invocation -> {
                    final String taxCode = invocation.getArgument(0, String.class);
                    if (userInfo1.getTaxCode().equals(taxCode)) {
                        return Optional.empty();
                    } else {
                        final it.pagopa.selfcare.external_api.model.user.User user = new it.pagopa.selfcare.external_api.model.user.User();
                        final CertifiedField<String> name = new CertifiedField<>();
                        name.setCertification(Certification.NONE);
                        name.setValue("setName3");
                        user.setName(name);
                        final CertifiedField<String> familyName = new CertifiedField<>();
                        familyName.setCertification(Certification.SPID);
                        familyName.setValue("setSurname2");
                        user.setFamilyName(familyName);
                        final CertifiedField<String> email = new CertifiedField<>();
                        email.setCertification(Certification.NONE);
                        email.setValue("setEmail1");
                        final WorkContact workContact = new WorkContact();
                        workContact.setEmail(email);
                        user.setWorkContacts(Map.of("differentKey", workContact));
                        user.setId(UUID.randomUUID().toString());
                        return Optional.of(user);
                    }
                });
        when(userRegistryConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        //when
        final Executable executable = () -> onboardingServiceImpl.autoApprovalOnboarding(onboardingData);
        //then
        assertDoesNotThrow(executable);
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingData.getInstitutionExternalId(), onboardingData.getProductId());
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .createInstitutionUsingExternalId(onboardingData.getInstitutionExternalId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingData.getProductId(), onboardingData.getInstitutionExternalId());
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        verify(userRegistryConnectorMock, times(1))
                .saveUser(saveUserCaptor.capture());
        onboardingData.getUsers().forEach(user ->
                verify(userRegistryConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verify(userRegistryConnectorMock, times(1))
                .updateUser(any(), any());
        verify(partyConnectorMock, times(1))
                .autoApprovalOnboarding(any());
        verifyNoMoreInteractions(productsConnectorMock, onboardingValidationStrategyMock);
    }

    @Test
    void autoApprovalOnboarding_subProduct_notAllowed() {
        //given
        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setUsers");
        Product baseProductMock = mockInstance(new Product(), 1, "setParentId");
        Product subProductMock = mockInstance(new Product(), 2, "setId", "setParentId", "setRoleMappings");
        subProductMock.setId(onboardingData.getProductId());
        subProductMock.setParentId(baseProductMock.getId());
        onboardingData.setProductId(subProductMock.getId());
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenThrow(InstitutionDoesNotExistException.class);
        when(productsConnectorMock.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType()))
                .thenReturn(subProductMock);
        when(productsConnectorMock.getProduct(subProductMock.getParentId(), null))
                .thenReturn(baseProductMock);
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(false);
        //when
        Executable executable = () -> onboardingServiceImpl.autoApprovalOnboarding(onboardingData);
        //then
        Exception e = assertThrows(OnboardingNotAllowedException.class, executable);
        assertEquals("Institution with external id '" + onboardingData.getInstitutionExternalId() + "' is not allowed to onboard '" + baseProductMock.getId() + "' product",
                e.getMessage());
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingData.getInstitutionExternalId(), onboardingData.getProductId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        verify(productsConnectorMock, times(1))
                .getProduct(subProductMock.getParentId(), null);
        verify(onboardingValidationStrategyMock, times(1))
                .validate(baseProductMock.getId(), onboardingData.getInstitutionExternalId());
        verifyNoMoreInteractions(productsConnectorMock, onboardingValidationStrategyMock);
        verifyNoInteractions(userRegistryConnectorMock);
    }

    @Test
    void autoApprovalOnboarding_noManagaerFoundForSubProduct() {
        //given
        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setUsers");
        Product baseProductMock = mockInstance(new Product(), 1, "setParentId");
        Product subProductMock = mockInstance(new Product(), 2, "setParentId", "setRoleMappings");
        subProductMock.setParentId(baseProductMock.getId());
        onboardingData.setProductId(subProductMock.getId());
        when(partyConnectorMock.verifyOnboarding(onboardingData.getInstitutionExternalId(), onboardingData.getProductId()))
                .thenThrow(InstitutionDoesNotExistException.class);
        when(productsConnectorMock.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType()))
                .thenReturn(subProductMock);
        when(productsConnectorMock.getProduct(subProductMock.getParentId(), null))
                .thenReturn(baseProductMock);
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        doThrow(RuntimeException.class)
                .when(partyConnectorMock)
                .verifyOnboarding(onboardingData.getInstitutionExternalId(), subProductMock.getParentId());
        //when
        Executable executable = () -> onboardingServiceImpl.autoApprovalOnboarding(onboardingData);
        //then
        ValidationException e = assertThrows(ValidationException.class, executable);
        assertEquals("Unable to complete the onboarding for institution with external id '" + onboardingData.getInstitutionExternalId() + "' to product '" + subProductMock.getId() + "'. Please onboard first the '" + subProductMock.getParentId() + "' product for the same institution", e.getMessage());
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingData.getInstitutionExternalId(), onboardingData.getProductId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        verify(productsConnectorMock, times(1))
                .getProduct(subProductMock.getParentId(), null);
        verify(onboardingValidationStrategyMock, times(1))
                .validate(baseProductMock.getId(), onboardingData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingData.getInstitutionExternalId(), baseProductMock.getId());
        verifyNoMoreInteractions(partyConnectorMock, productsConnectorMock, onboardingValidationStrategyMock);
        verifyNoInteractions(userRegistryConnectorMock);
    }

    @Test
    void autoApprovalOnboardingSubProduct() {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);
        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setUsers");
        onboardingData.setUsers(List.of(userInfo1, userInfo2));
        Product baseProductMock = mockInstance(new Product(), "setRoleMappings", "setParentId");
        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRoleInfo.ProductRole productRole1 = mockInstance(new ProductRoleInfo.ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRoleInfo.ProductRole productRole2 = mockInstance(new ProductRoleInfo.ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        baseProductMock.setRoleMappings(roleMappings);
        Product subProductMock = mockInstance(new Product(), "setId");
        subProductMock.setId(onboardingData.getProductId());
        subProductMock.setParentId(baseProductMock.getId());
        onboardingData.setProductId(subProductMock.getId());
        when(partyConnectorMock.verifyOnboarding(onboardingData.getInstitutionExternalId(), onboardingData.getProductId()))
                .thenThrow(InstitutionDoesNotExistException.class);
        when(productsConnectorMock.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType()))
                .thenReturn(subProductMock);
        when(productsConnectorMock.getProduct(subProductMock.getParentId(), null))
                .thenReturn(baseProductMock);
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        UserInfo managerInfo = mockInstance(new UserInfo());
        managerInfo.setInstitutionId(institution.getId());
        when(partyConnectorMock.getInstitutionByExternalId(anyString()))
                .thenReturn(institution);
        when(userRegistryConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        // when
        onboardingServiceImpl.autoApprovalOnboarding(onboardingData);
        // then
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingData.getInstitutionExternalId(), onboardingData.getProductId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingData.getInstitutionExternalId(), baseProductMock.getId());
        verify(productsConnectorMock, times(1))
                .getProduct(subProductMock.getParentId(), null);
        verify(partyConnectorMock, times(1))
                .autoApprovalOnboarding(onboardingDataCaptor.capture());
        OnboardingData captured = onboardingDataCaptor.getValue();
        assertNotNull(captured.getUsers());
        assertEquals(onboardingData.getUsers().size(), captured.getUsers().size());
        captured.getUsers().forEach(userInfo -> {
            assertEquals(productRole, userInfo.getProductRole());
            assertNotNull(userInfo.getId());
            checkNotNullFields(userInfo);
        });
        onboardingData.getUsers().forEach(user ->
                verify(userRegistryConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        verify(userRegistryConnectorMock, times(onboardingData.getUsers().size()))
                .saveUser(saveUserCaptor.capture());
        List<SaveUserDto> savedUsers = saveUserCaptor.getAllValues();
        savedUsers.forEach(saveUserDto -> assertTrue(saveUserDto.getWorkContacts().containsKey(institution.getId())));
        verify(onboardingValidationStrategyMock, times(1))
                .validate(baseProductMock.getId(), onboardingData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingData.getInstitutionExternalId());
        verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock, userRegistryConnectorMock, onboardingValidationStrategyMock);
    }

    @Test
    void autoApprovalOnboarding_GSP_prodInterop_originIPA() {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);
        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setInstitutionType", "setUsers", "setOrigin", "setProductId");
        onboardingData.setInstitutionType(InstitutionType.GSP);
        onboardingData.setOrigin("IPA");
        onboardingData.setProductId("prod-interop");
        onboardingData.setUsers(List.of(userInfo1, userInfo2));
        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId", "setId");
        productMock.setId(onboardingData.getProductId());
        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRoleInfo.ProductRole productRole1 = mockInstance(new ProductRoleInfo.ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRoleInfo.ProductRole productRole2 = mockInstance(new ProductRoleInfo.ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        when(partyConnectorMock.verifyOnboarding(onboardingData.getInstitutionExternalId(), onboardingData.getProductId()))
                .thenThrow(InstitutionDoesNotExistException.class);
        when(partyConnectorMock.getInstitutionByExternalId(anyString()))
                .thenThrow(ResourceNotFoundException.class);
        when(partyConnectorMock.createInstitutionUsingExternalId(anyString()))
                .thenReturn(institution);
        productMock.setRoleMappings(roleMappings);
        when(productsConnectorMock.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType()))
                .thenReturn(productMock);
        when(userRegistryConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        // when
        onboardingServiceImpl.autoApprovalOnboarding(onboardingData);
        // then
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingData.getInstitutionExternalId(), onboardingData.getProductId());
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .createInstitutionUsingExternalId(onboardingData.getInstitutionExternalId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingData.getProductId(), onboardingData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .autoApprovalOnboarding(onboardingDataCaptor.capture());
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        onboardingData.getUsers().forEach(user ->
                verify(userRegistryConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verify(userRegistryConnectorMock, times(2))
                .saveUser(saveUserCaptor.capture());
        List<SaveUserDto> savedUsers = saveUserCaptor.getAllValues();
        savedUsers.forEach(saveUserDto -> assertTrue(saveUserDto.getWorkContacts().containsKey(institution.getId())));
        OnboardingData captured = onboardingDataCaptor.getValue();
        assertNotNull(captured.getUsers());
        assertEquals(2, captured.getUsers().size());
        captured.getUsers().forEach(userInfo -> {
            assertEquals(productRole, userInfo.getProductRole());
            assertNotNull(userInfo.getId());
        });
        verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock, userRegistryConnectorMock, onboardingValidationStrategyMock);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "PSP,SELC,prod-io",
            "PSP,SELC,prod-interop",
            "PSP,IPA,prod-io",
            "PSP,IPA,prod-interop",
            "GSP,SELC,prod-io",
            "GSP,SELC,prod-interop",
            "GSP,IPA,prod-io"
    })
    void onboarding_GSP_prodInterop_originIPA_failingConditions(InstitutionType institutionType, String origin, String productId) {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);
        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setInstitutionType", "setUsers", "setOrigin", "setProductId");
        onboardingData.setInstitutionType(institutionType);
        onboardingData.setOrigin(origin);
        onboardingData.setProductId(productId);
        onboardingData.setUsers(List.of(userInfo1, userInfo2));
        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId", "setId");
        productMock.setId(onboardingData.getProductId());
        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRoleInfo.ProductRole productRole1 = mockInstance(new ProductRoleInfo.ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRoleInfo.ProductRole productRole2 = mockInstance(new ProductRoleInfo.ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        when(partyConnectorMock.verifyOnboarding(onboardingData.getInstitutionExternalId(), onboardingData.getProductId()))
                .thenThrow(InstitutionDoesNotExistException.class);
        when(partyConnectorMock.getInstitutionByExternalId(anyString()))
                .thenThrow(ResourceNotFoundException.class);
        when(partyConnectorMock.createInstitutionRaw(any()))
                .thenReturn(institution);
        productMock.setRoleMappings(roleMappings);
        when(productsConnectorMock.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType()))
                .thenReturn(productMock);
        when(userRegistryConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        // when
        onboardingServiceImpl.autoApprovalOnboarding(onboardingData);
        // then
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(onboardingData.getInstitutionExternalId(), onboardingData.getProductId());
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .createInstitutionRaw(onboardingData);
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingData.getProductId(), onboardingData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .autoApprovalOnboarding(onboardingDataCaptor.capture());
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        onboardingData.getUsers().forEach(user ->
                verify(userRegistryConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verify(userRegistryConnectorMock, times(2))
                .saveUser(saveUserCaptor.capture());
        List<SaveUserDto> savedUsers = saveUserCaptor.getAllValues();
        savedUsers.forEach(saveUserDto -> assertTrue(saveUserDto.getWorkContacts().containsKey(institution.getId())));
        OnboardingData captured = onboardingDataCaptor.getValue();
        assertNotNull(captured.getUsers());
        assertEquals(2, captured.getUsers().size());
        captured.getUsers().forEach(userInfo -> {
            assertEquals(productRole, userInfo.getProductRole());
            assertNotNull(userInfo.getId());
        });
        verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock, userRegistryConnectorMock, onboardingValidationStrategyMock);
    }

    @Test
    void verifyOnboarding() {
        // given
        ResponseEntity<Void> responseEntityMock = new ResponseEntity<>(HttpStatus.NO_CONTENT);
        String externalInstitutionIdMock = "externalInstitutionId";
        String productIdMock = "productId";
        when(partyConnectorMock.verifyOnboarding(any(), any()))
                .thenReturn(responseEntityMock);
        // when
        ResponseEntity<Void> response = onboardingServiceImpl.verifyOnboarding(externalInstitutionIdMock, productIdMock);
        // then
        assertNotNull(response);
        assertEquals(response, responseEntityMock);
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(externalInstitutionIdMock, productIdMock);
        verifyNoMoreInteractions(partyConnectorMock);
        verifyNoInteractions(userRegistryConnectorMock, productsConnectorMock, onboardingValidationStrategyMock);
    }

    @Test
    void shouldOnboardingProductInstitutionNotPa() {
        // given
        String productRole = "role";
        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setInstitutionType", "setUsers");
        onboardingData.setInstitutionType(InstitutionType.PSP);
        onboardingData.setUsers(List.of(dummyManager, dummyDelegate));

        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRoleInfo.ProductRole productRole1 = mockInstance(new ProductRoleInfo.ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRoleInfo.ProductRole productRole2 = mockInstance(new ProductRoleInfo.ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};

        Product productMock = new Product();
        productMock.setId(onboardingData.getProductId());
        productMock.setRoleMappings(roleMappings);

        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());

        when(partyConnectorMock.getInstitutionsByTaxCodeAndSubunitCode(anyString(),anyString()))
                .thenThrow(ResourceNotFoundException.class);
        when(partyConnectorMock.createInstitution(any())).thenReturn(institution);

        when(productsConnectorMock.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType()))
                .thenReturn(productMock);
        when(userRegistryConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        // when
        onboardingServiceImpl.autoApprovalOnboardingProduct(onboardingData);
        // then
        verify(partyConnectorMock, times(1))
                .getInstitutionsByTaxCodeAndSubunitCode(onboardingData.getTaxCode(), onboardingData.getSubunitCode());
        verify(partyConnectorMock, times(1))
                .createInstitution(onboardingData);
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingData.getProductId(), onboardingData.getTaxCode());
        verify(partyConnectorMock, times(1))
                .autoApprovalOnboarding(onboardingDataCaptor.capture());
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        onboardingData.getUsers().forEach(user ->
                verify(userRegistryConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verify(userRegistryConnectorMock, times(2))
                .saveUser(saveUserCaptor.capture());
        List<SaveUserDto> savedUsers = saveUserCaptor.getAllValues();
        savedUsers.forEach(saveUserDto -> assertTrue(saveUserDto.getWorkContacts().containsKey(institution.getId())));
        OnboardingData captured = onboardingDataCaptor.getValue();
        assertNotNull(captured.getUsers());
        assertEquals(2, captured.getUsers().size());
        captured.getUsers().forEach(userInfo -> {
            assertEquals(productRole, userInfo.getProductRole());
            assertNotNull(userInfo.getId());
        });
        verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock, userRegistryConnectorMock, onboardingValidationStrategyMock);
    }

    @Test
    void shouldOnboardingProductInstitutionPa() {
        // given
        String productRole = "role";

        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setInstitutionType", "setUsers");
        onboardingData.setInstitutionType(InstitutionType.PA);
        onboardingData.setUsers(List.of(dummyManager, dummyDelegate));
        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId", "setId");
        productMock.setId(onboardingData.getProductId());
        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRoleInfo.ProductRole productRole1 = mockInstance(new ProductRoleInfo.ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRoleInfo.ProductRole productRole2 = mockInstance(new ProductRoleInfo.ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        when(partyConnectorMock.getInstitutionsByTaxCodeAndSubunitCode(anyString(),anyString()))
                .thenThrow(ResourceNotFoundException.class);
        when(partyConnectorMock.createInstitutionFromIpa(anyString(),anyString(),anyString()))
                .thenReturn(institution);
        productMock.setRoleMappings(roleMappings);
        when(productsConnectorMock.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType()))
                .thenReturn(productMock);
        when(userRegistryConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        // when
        onboardingServiceImpl.autoApprovalOnboardingProduct(onboardingData);
        // then
        verify(partyConnectorMock, times(1))
                .getInstitutionsByTaxCodeAndSubunitCode(onboardingData.getTaxCode(), onboardingData.getSubunitCode());
        verify(partyConnectorMock, times(1))
                .createInstitutionFromIpa(onboardingData.getTaxCode(), onboardingData.getSubunitCode(), onboardingData.getSubunitType());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingData.getProductId(), onboardingData.getTaxCode());
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        onboardingData.getUsers().forEach(user ->
                verify(userRegistryConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verify(userRegistryConnectorMock, times(2))
                .saveUser(saveUserCaptor.capture());
        verify(partyConnectorMock, times(1))
                .autoApprovalOnboarding(onboardingDataCaptor.capture());
        List<SaveUserDto> savedUsers = saveUserCaptor.getAllValues();
        savedUsers.forEach(saveUserDto -> assertTrue(saveUserDto.getWorkContacts().containsKey(institution.getId())));
        OnboardingData captured = onboardingDataCaptor.getValue();
        assertNotNull(captured.getUsers());
        assertEquals(2, captured.getUsers().size());
        captured.getUsers().forEach(userInfo -> {
            assertEquals(productRole, userInfo.getProductRole());
            assertNotNull(userInfo.getId());
        });
        verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock, userRegistryConnectorMock, onboardingValidationStrategyMock);
    }

    @Test
    void shouldOnboardingProductInstitutionAnac() {
        // given
        String productRole = "role";

        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setInstitutionType", "setUsers");
        onboardingData.setInstitutionType(InstitutionType.SA);
        onboardingData.setOrigin("ANAC");
        onboardingData.setUsers(List.of(dummyManager, dummyDelegate));
        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId", "setId");
        productMock.setId(onboardingData.getProductId());
        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRoleInfo.ProductRole productRole1 = mockInstance(new ProductRoleInfo.ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRoleInfo.ProductRole productRole2 = mockInstance(new ProductRoleInfo.ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        when(partyConnectorMock.getInstitutionsByTaxCodeAndSubunitCode(anyString(),anyString()))
                .thenThrow(ResourceNotFoundException.class);
        when(partyConnectorMock.createInstitutionFromANAC(onboardingData))
                .thenReturn(institution);
        productMock.setRoleMappings(roleMappings);
        when(productsConnectorMock.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType()))
                .thenReturn(productMock);
        when(userRegistryConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        // when
        onboardingServiceImpl.autoApprovalOnboardingProduct(onboardingData);
        // then
        verify(partyConnectorMock, times(1))
                .getInstitutionsByTaxCodeAndSubunitCode(onboardingData.getTaxCode(), onboardingData.getSubunitCode());
        verify(partyConnectorMock, times(1))
                .createInstitutionFromANAC(onboardingData);
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingData.getProductId(), onboardingData.getTaxCode());
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        onboardingData.getUsers().forEach(user ->
                verify(userRegistryConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verify(userRegistryConnectorMock, times(2))
                .saveUser(saveUserCaptor.capture());
        verify(partyConnectorMock, times(1))
                .autoApprovalOnboarding(onboardingDataCaptor.capture());
        List<SaveUserDto> savedUsers = saveUserCaptor.getAllValues();
        savedUsers.forEach(saveUserDto -> assertTrue(saveUserDto.getWorkContacts().containsKey(institution.getId())));
        OnboardingData captured = onboardingDataCaptor.getValue();
        assertNotNull(captured.getUsers());
        assertEquals(2, captured.getUsers().size());
        captured.getUsers().forEach(userInfo -> {
            assertEquals(productRole, userInfo.getProductRole());
            assertNotNull(userInfo.getId());
        });
        verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock, userRegistryConnectorMock, onboardingValidationStrategyMock);
    }


    @Test
    void onboardingProduct_PhaseOutException() {
        // given
        OnboardingData onboardingData = mockInstance(new OnboardingData());
        onboardingData.setProductId("id");
        onboardingData.setInstitutionExternalId("externalId");
        Product product = new Product();
        product.setId("id");
        product.setStatus(ProductStatus.PHASE_OUT);

        when(productsConnectorMock.getProduct(anyString(), any()))
                .thenReturn(product);
        // when
        Executable executable = () -> onboardingServiceImpl.autoApprovalOnboardingProduct(onboardingData);
        // then
        ValidationException e = assertThrows(ValidationException.class, executable);
        assertEquals(String.format("Unable to complete the onboarding for institution with external id '%s' to product '%s', the product is dismissed.",
                        onboardingData.getInstitutionExternalId(),
                        product.getId()),
                e.getMessage());
        verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock, msPartyRegistryProxyConnectorMock);
    }

    @Test
    void onboardingParentProduct_PhaseOutException() {
        // given
        OnboardingData onboardingData = mockInstance(new OnboardingData());
        onboardingData.setProductId("id");
        onboardingData.setInstitutionExternalId("externalId");
        onboardingData.setTaxCode("taxCode");
        onboardingData.setInstitutionType(InstitutionType.PT);
        Product product = new Product();
        product.setId("id");
        product.setStatus(ProductStatus.ACTIVE);
        product.setParentId("parentId");
        Product parent = new Product();
        parent.setParentId("parentId");
        parent.setStatus(ProductStatus.PHASE_OUT);

        when(productsConnectorMock.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType()))
                .thenReturn(product);
        when(productsConnectorMock.getProduct("parentId", null)).thenReturn(parent);


        // when
        Executable executable = () -> onboardingServiceImpl.autoApprovalOnboardingProduct(onboardingData);
        // then
        ValidationException e = assertThrows(ValidationException.class, executable);
        assertEquals(String.format("Unable to complete the onboarding for institution with taxCode '%s' to product '%s', the base product is dismissed.",
                        onboardingData.getTaxCode(),
                        parent.getId()),
                e.getMessage());
        verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock, msPartyRegistryProxyConnectorMock);
    }

    @Test
    void onboardingValidationException() {
        // given
        OnboardingData onboardingData = mockInstance(new OnboardingData());
        onboardingData.setProductId("id");
        onboardingData.setInstitutionExternalId("externalId");
        onboardingData.setTaxCode("taxCode");
        onboardingData.setInstitutionType(InstitutionType.PT);
        Product product = new Product();
        product.setId("id");
        product.setStatus(ProductStatus.ACTIVE);
        product.setParentId("parentId");
        Product parent = new Product();
        parent.setParentId("parentId");
        parent.setStatus(ProductStatus.ACTIVE);
        //when
        when(productsConnectorMock.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType()))
                .thenReturn(product);
        when(productsConnectorMock.getProduct("parentId", null)).thenReturn(parent);
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        Executable executable = () -> onboardingServiceImpl.autoApprovalOnboardingProduct(onboardingData);
        // then
        ValidationException e = assertThrows(ValidationException.class, executable);
        assertEquals(String.format("Unable to complete the onboarding for institution with taxCode '%s' to product '%s'. Please onboard first the '%s' product for the same institution",
                        onboardingData.getTaxCode(),
                        product.getId(),
                        parent.getId()),
                e.getMessage());

        verifyNoMoreInteractions(productsConnectorMock, msPartyRegistryProxyConnectorMock, onboardingValidationStrategyMock);
    }

    @Test
    void onboardingValidationExceptionForProduct() {
        // given
        OnboardingData onboardingData = mockInstance(new OnboardingData());
        onboardingData.setProductId("id");
        onboardingData.setInstitutionExternalId("externalId");
        onboardingData.setTaxCode("taxCode");
        onboardingData.setSubunitCode("subunitCode");
        onboardingData.setInstitutionType(InstitutionType.PT);
        Product product = new Product();
        product.setId("id");
        product.setStatus(ProductStatus.ACTIVE);
        product.setParentId("parentId");
        Product parent = new Product();
        parent.setParentId("parentId");
        parent.setStatus(ProductStatus.ACTIVE);

        when(productsConnectorMock.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType()))
                .thenReturn(product);
        when(productsConnectorMock.getProduct("parentId", null)).thenReturn(parent);
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.NO_CONTENT);
        when(partyConnectorMock.verifyOnboarding(anyString(), anyString(), any())).thenReturn(responseEntity);

        // when
        Executable executable = () -> onboardingServiceImpl.autoApprovalOnboardingProduct(onboardingData);
        // then
        ValidationException e = assertThrows(ValidationException.class, executable);
        assertEquals(String.format("Unable to complete the onboarding for institution with taxCode '%s' to product '%s'. Please onboard first the '%s' product for the same institution",
                        onboardingData.getTaxCode(),
                        product.getId(),
                        parent.getId()),
                e.getMessage());

        verifyNoMoreInteractions(productsConnectorMock, msPartyRegistryProxyConnectorMock, onboardingValidationStrategyMock);



    }

    @Test
    void shouldOnboardingPdaInstitutionForEc1() {
        // given
        String productRole = "role";
        PdaOnboardingData onboardingData = mockInstance(new PdaOnboardingData(), "setInstitutionType", "setUsers");
        onboardingData.setUsers(List.of(dummyManager, dummyDelegate));

        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRoleInfo.ProductRole productRole1 = mockInstance(new ProductRoleInfo.ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRoleInfo.ProductRole productRole2 = mockInstance(new ProductRoleInfo.ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};

        Product productMock = new Product();
        productMock.setId(onboardingData.getProductId());
        productMock.setRoleMappings(roleMappings);

        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        institution.setInstitutionType(InstitutionType.PA);

        when(partyConnectorMock.getInstitutionsByTaxCodeAndSubunitCode(anyString(),any()))
                .thenThrow(ResourceNotFoundException.class);
        when(partyConnectorMock.createInstitutionFromPda(any())).thenReturn(institution);

        when(productsConnectorMock.getProduct(onboardingData.getProductId(), null))
                .thenReturn(productMock);
        when(userRegistryConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        // when
        onboardingServiceImpl.autoApprovalOnboardingFromPda(onboardingData, "EC");
        // then
        verify(partyConnectorMock, times(1))
                .createInstitutionFromPda(onboardingData);
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), null);
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingData.getProductId(), onboardingData.getTaxCode());
        verify(partyConnectorMock, times(1))
                .autoApprovalOnboarding(onboardingDataCaptor.capture());
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        onboardingData.getUsers().forEach(user ->
                verify(userRegistryConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verify(userRegistryConnectorMock, times(2))
                .saveUser(saveUserCaptor.capture());
        List<SaveUserDto> savedUsers = saveUserCaptor.getAllValues();
        savedUsers.forEach(saveUserDto -> assertTrue(saveUserDto.getWorkContacts().containsKey(institution.getId())));
        OnboardingData captured = onboardingDataCaptor.getValue();
        assertNotNull(captured.getUsers());
        assertEquals(2, captured.getUsers().size());
        captured.getUsers().forEach(userInfo -> {
            assertEquals(productRole, userInfo.getProductRole());
            assertNotNull(userInfo.getId());
        });
        verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock, userRegistryConnectorMock, onboardingValidationStrategyMock);
    }

    @Test
    void shouldOnboardingPdaInstitutionForEc2() {
        // given
        String productRole = "role";
        PdaOnboardingData onboardingData = mockInstance(new PdaOnboardingData(), "setInstitutionType", "setUsers");
        onboardingData.setUsers(List.of(dummyManager, dummyDelegate));

        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRoleInfo.ProductRole productRole1 = mockInstance(new ProductRoleInfo.ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRoleInfo.ProductRole productRole2 = mockInstance(new ProductRoleInfo.ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};

        Product productMock = new Product();
        productMock.setId(onboardingData.getProductId());
        productMock.setRoleMappings(roleMappings);

        Institution institution = mockInstance(new Institution(), "setSubunitCode");
        institution.setId(UUID.randomUUID().toString());
        institution.setInstitutionType(InstitutionType.PA);

        when(partyConnectorMock.getInstitutionsByTaxCodeAndSubunitCode(anyString(),any()))
                .thenReturn(List.of(institution));

        when(productsConnectorMock.getProduct(onboardingData.getProductId(), null))
                .thenReturn(productMock);
        when(userRegistryConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        // when
        onboardingServiceImpl.autoApprovalOnboardingFromPda(onboardingData, "EC");
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), null);
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingData.getProductId(), onboardingData.getTaxCode());
        verify(partyConnectorMock, times(1))
                .autoApprovalOnboarding(onboardingDataCaptor.capture());
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        onboardingData.getUsers().forEach(user ->
                verify(userRegistryConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verify(userRegistryConnectorMock, times(2))
                .saveUser(saveUserCaptor.capture());
        List<SaveUserDto> savedUsers = saveUserCaptor.getAllValues();
        savedUsers.forEach(saveUserDto -> assertTrue(saveUserDto.getWorkContacts().containsKey(institution.getId())));
        OnboardingData captured = onboardingDataCaptor.getValue();
        assertNotNull(captured.getUsers());
        assertEquals(2, captured.getUsers().size());
        captured.getUsers().forEach(userInfo -> {
            assertEquals(productRole, userInfo.getProductRole());
            assertNotNull(userInfo.getId());
        });
        verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock, userRegistryConnectorMock, onboardingValidationStrategyMock);
    }

    @Test
    void shouldOnboardingPdaInstitutionForPt() {
        // given
        String productRole = "role";
        PdaOnboardingData onboardingData = mockInstance(new PdaOnboardingData(), "setInstitutionType", "setUsers");
        onboardingData.setUsers(List.of(dummyManager, dummyDelegate));

        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRoleInfo.ProductRole productRole1 = mockInstance(new ProductRoleInfo.ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRoleInfo.ProductRole productRole2 = mockInstance(new ProductRoleInfo.ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};

        Product productMock = new Product();
        productMock.setId(onboardingData.getProductId());
        productMock.setRoleMappings(roleMappings);

        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        institution.setInstitutionType(InstitutionType.PT);

        when(partyConnectorMock.getInstitutionsByTaxCodeAndSubunitCode(anyString(),any()))
                .thenThrow(ResourceNotFoundException.class);
        when(partyConnectorMock.createInstitutionFromPda(any())).thenReturn(institution);

        when(productsConnectorMock.getProduct(onboardingData.getProductId(), null))
                .thenReturn(productMock);
        when(userRegistryConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        // when
        onboardingServiceImpl.autoApprovalOnboardingFromPda(onboardingData, "PT");
        // then
        verify(partyConnectorMock, times(1))
                .createInstitutionFromPda(onboardingData);
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), null);
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingData.getProductId(), onboardingData.getTaxCode());
        verify(partyConnectorMock, times(1))
                .autoApprovalOnboarding(onboardingDataCaptor.capture());
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        onboardingData.getUsers().forEach(user ->
                verify(userRegistryConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verify(userRegistryConnectorMock, times(2))
                .saveUser(saveUserCaptor.capture());
        List<SaveUserDto> savedUsers = saveUserCaptor.getAllValues();
        savedUsers.forEach(saveUserDto -> assertTrue(saveUserDto.getWorkContacts().containsKey(institution.getId())));
        OnboardingData captured = onboardingDataCaptor.getValue();
        assertNotNull(captured.getUsers());
        assertEquals(2, captured.getUsers().size());
        captured.getUsers().forEach(userInfo -> {
            assertEquals(productRole, userInfo.getProductRole());
            assertNotNull(userInfo.getId());
        });
        verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock, userRegistryConnectorMock, onboardingValidationStrategyMock);
    }

}
