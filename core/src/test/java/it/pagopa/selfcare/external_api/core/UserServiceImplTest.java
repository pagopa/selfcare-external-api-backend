package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.commons.base.utils.ProductId;
import it.pagopa.selfcare.external_api.api.MsCoreConnector;
import it.pagopa.selfcare.external_api.api.UserMsConnector;
import it.pagopa.selfcare.external_api.api.UserRegistryConnector;
import it.pagopa.selfcare.external_api.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionInfo;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionResponse;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingInfoResponse;
import it.pagopa.selfcare.external_api.model.onboarding.ProductInfo;
import it.pagopa.selfcare.external_api.model.onboarding.mapper.OnboardingInstitutionMapper;
import it.pagopa.selfcare.external_api.model.onboarding.mapper.OnboardingInstitutionMapperImpl;
import it.pagopa.selfcare.external_api.model.user.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.test.context.TestSecurityContextHolder;

import java.time.LocalDateTime;
import java.util.*;

import static it.pagopa.selfcare.commons.utils.TestUtils.checkNotNullFields;
import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRegistryConnector userRegistryConnector;

    @Mock
    private UserMsConnector userMsConnector;


    @Mock
    private MsCoreConnector msCoreConnector;

    @Spy
    OnboardingInstitutionMapper onboardingInstitutionMapper = new OnboardingInstitutionMapperImpl();

    private static final String fiscalCode = "MNCCSD01R13A757G";

    @BeforeEach
    void beforeEach() {
        TestSecurityContextHolder.clearContext();
    }

    private final static User dummyUser;

    static {
        dummyUser = new User();
        dummyUser.setFiscalCode("MNCCSD01R13A757G");
        dummyUser.setId("id");
        CertifiedField<String> fieldName = new CertifiedField<>();
        fieldName.setCertification(Certification.SPID);
        fieldName.setValue("testName");
        CertifiedField<String> fieldEmail = new CertifiedField<>();
        fieldEmail.setCertification(Certification.SPID);
        fieldEmail.setValue("test@email.com");
        dummyUser.setEmail(fieldEmail);
        dummyUser.setName(fieldName);
    }

    @Test
    void getUserInfo() {
        //given
        Optional<User> optUser = Optional.of(dummyUser);
        // when
        when(userRegistryConnector.search(anyString(), any()))
                .thenReturn(optUser);
        OnboardingInfoResponse onboardingInfoResponse = buildOnboardingInfoResponse();
        when(msCoreConnector.getInstitutionProductsInfo("id", List.of(RelationshipState.ACTIVE))).thenReturn(onboardingInfoResponse);
        UserInfoWrapper userWrapper = userService.getUserInfo(fiscalCode, List.of(RelationshipState.ACTIVE));
        // then
        assertNotNull(userWrapper);
        assertNotNull(userWrapper.getUser());
        assertNotNull(userWrapper.getOnboardedInstitutions());
        assertEquals(1, userWrapper.getOnboardedInstitutions().size());
        assertEquals(optUser.get().getName().getValue(),  userWrapper.getUser().getName().getValue());
        assertEquals(optUser.get().getEmail().getValue(),  userWrapper.getUser().getEmail().getValue());
        assertEquals(onboardingInfoResponse.getInstitutions().get(0).getAddress(), userWrapper.getOnboardedInstitutions().get(0).getAddress());
    }

    @Test
    void getUserInfoWithWorkContacts() {
        //given
        WorkContact workContact = new WorkContact();
        CertifiedField<String> email = new CertifiedField<>();
        email.setValue("test@test.it");
        workContact.setEmail(email);
        dummyUser.setWorkContacts(Map.of("id", workContact));
        Optional<User> optUser = Optional.of(dummyUser);

        // when
        when(userRegistryConnector.search(anyString(), any()))
                .thenReturn(optUser);
        OnboardingInfoResponse onboardingInfoResponse = buildOnboardingInfoResponse();
        when(msCoreConnector.getInstitutionProductsInfo("id", List.of(RelationshipState.ACTIVE))).thenReturn(onboardingInfoResponse);
        UserInfoWrapper userWrapper = userService.getUserInfo(fiscalCode, List.of(RelationshipState.ACTIVE));
        // then
        assertNotNull(userWrapper);
        assertNotNull(userWrapper.getUser());
        assertNotNull(userWrapper.getOnboardedInstitutions());
        assertEquals(1, userWrapper.getOnboardedInstitutions().size());
        assertEquals(optUser.get().getName().getValue(),  userWrapper.getUser().getName().getValue());
        assertEquals(optUser.get().getEmail().getValue(),  userWrapper.getUser().getEmail().getValue());
        assertEquals(onboardingInfoResponse.getInstitutions().get(0).getAddress(), userWrapper.getOnboardedInstitutions().get(0).getAddress());
        assertEquals("test@test.it", userWrapper.getOnboardedInstitutions().get(0).getUserEmail());
    }

    @Test
    void getEmptyUser() {
        //given
        Optional<User> optUser = Optional.empty();
        // when
        when(userRegistryConnector.search(anyString(), any()))
                .thenReturn(optUser);

        Executable executable = () -> userService.getUserInfo(fiscalCode, List.of(RelationshipState.ACTIVE));
        // then
        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, executable);
        assertEquals("User with fiscal code " + fiscalCode + " not found", e.getMessage());

    }

    private OnboardingInfoResponse buildOnboardingInfoResponse() {
        OnboardingInfoResponse response = new OnboardingInfoResponse();
        OnboardedInstitutionResponse institutionResponse = new OnboardedInstitutionResponse();
        institutionResponse.setId("id");
        institutionResponse.setAddress("address");
        response.setInstitutions(List.of(institutionResponse));
        return response;
    }

    @Test
    void getUserOnboardedProductDetails(){
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        String userId = "userId";
        OnboardingInfoResponse onboardingInfoResponse = new OnboardingInfoResponse();
        onboardingInfoResponse.setUserId(userId);
        OnboardedInstitutionResponse institutionResponse = new OnboardedInstitutionResponse();
        institutionResponse.setId(institutionId);
        ProductInfo productInfo = mockInstance(new ProductInfo());
        productInfo.setId(productId);
        institutionResponse.setProductInfo(productInfo);
        institutionResponse.setRole(PartyRole.MANAGER);
        onboardingInfoResponse.setInstitutions(List.of(institutionResponse));
        when(msCoreConnector.getInstitutionProductsInfo(anyString())).thenReturn(onboardingInfoResponse);
        //when
        UserDetailsWrapper result = userService.getUserOnboardedProductDetails(userId, institutionId, productId);
        //then
        checkNotNullFields(result);
        verify(msCoreConnector, times(1)).getInstitutionProductsInfo(userId);
    }

    @Test
    void getUserOnboardedProduct_noMatch(){
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        String userId = "userId";
        OnboardingInfoResponse onboardingInfoResponse = new OnboardingInfoResponse();
        onboardingInfoResponse.setUserId(userId);
        OnboardedInstitutionResponse institutionResponse = new OnboardedInstitutionResponse();
        institutionResponse.setId("id");
        ProductInfo productInfo = mockInstance(new ProductInfo());
        productInfo.setId(productId);
        institutionResponse.setProductInfo(productInfo);
        institutionResponse.setRole(PartyRole.MANAGER);
        onboardingInfoResponse.setInstitutions(List.of(institutionResponse));
        when(msCoreConnector.getInstitutionProductsInfo(anyString())).thenReturn(onboardingInfoResponse);
        //when
        UserDetailsWrapper result = userService.getUserOnboardedProductDetails(userId, institutionId, productId);
        //then
        assertNull(result.getProductDetails());
        verify(msCoreConnector, times(1)).getInstitutionProductsInfo(userId);

    }

    @Test
    void getUserOnbaordedProductDetailsV2(){
        //given
        final String institutionId = "institutionId";
        final String userId = UUID.randomUUID().toString();
        final String productId = "prod-io";
        UserInstitution userInstitution = new UserInstitution();
        userInstitution.setInstitutionId(institutionId);
        OnboardedProductResponse onboardedProductResponse = new OnboardedProductResponse();
        onboardedProductResponse.setProductId(productId);
        onboardedProductResponse.setStatus("ACTIVE");
        onboardedProductResponse.setRole("MANAGER");
        onboardedProductResponse.setProductRole("admin");
        onboardedProductResponse.setCreatedAt(LocalDateTime.now());
        userInstitution.setProducts(List.of(onboardedProductResponse));
        when(userMsConnector.getUsersInstitutions(anyString(), any(), any(), any(), any(), any(), any(), any())).thenReturn(List.of(userInstitution));

        //when
        UserDetailsWrapper userDetailsWrapper = userService.getUserOnboardedProductsDetailsV2(userId, institutionId, productId);
        //then
        checkNotNullFields(userDetailsWrapper);
        verify(userMsConnector, times(1)).getUsersInstitutions(eq(userId), eq(institutionId), isNull(), isNull(), isNull(), eq(List.of(productId)), isNull(), isNull());
    }

    @Test
    void getUserOnbaordedProductDetailsV2_noMatchProduct(){
        //given
        final String institutionId = "institutionId";
        final String userId = UUID.randomUUID().toString();
        final String productId = "prod-io";
        UserInstitution userInstitution = new UserInstitution();
        userInstitution.setInstitutionId(institutionId);
        OnboardedProductResponse onboardedProductResponse = new OnboardedProductResponse();
        onboardedProductResponse.setProductId("prod");
        onboardedProductResponse.setStatus("ACTIVE");
        onboardedProductResponse.setRole("MANAGER");
        onboardedProductResponse.setProductRole("admin");
        onboardedProductResponse.setCreatedAt(LocalDateTime.now());
        userInstitution.setProducts(List.of(onboardedProductResponse));
        when(userMsConnector.getUsersInstitutions(anyString(), any(), any(), any(), any(), any(), any(), any())).thenReturn(List.of(userInstitution));

        //when
        UserDetailsWrapper userDetailsWrapper = userService.getUserOnboardedProductsDetailsV2(userId, institutionId, productId);
        //then
        assertNull(userDetailsWrapper.getProductDetails());
        verify(userMsConnector, times(1)).getUsersInstitutions(eq(userId), eq(institutionId), isNull(), isNull(), isNull(), eq(List.of(productId)), isNull(), isNull());
    }

    @Test
    void getUserOnbaordedProductDetailsV2_noMatchInstitution(){
        //given
        final String institutionId = "institutionId";
        final String userId = UUID.randomUUID().toString();
        final String productId = "prod-io";
        UserInstitution userInstitution = new UserInstitution();
        userInstitution.setInstitutionId("institution");
        OnboardedProductResponse onboardedProductResponse = new OnboardedProductResponse();
        onboardedProductResponse.setProductId("prod");
        onboardedProductResponse.setStatus("ACTIVE");
        onboardedProductResponse.setRole("MANAGER");
        onboardedProductResponse.setProductRole("admin");
        onboardedProductResponse.setCreatedAt(LocalDateTime.now());
        userInstitution.setProducts(List.of(onboardedProductResponse));
        when(userMsConnector.getUsersInstitutions(anyString(), any(), any(), any(), any(), any(), any(), any())).thenReturn(List.of(userInstitution));

        //when
        UserDetailsWrapper userDetailsWrapper = userService.getUserOnboardedProductsDetailsV2(userId, institutionId, productId);
        //then
        assertNull(userDetailsWrapper.getProductDetails());
        verify(userMsConnector, times(1)).getUsersInstitutions(eq(userId), eq(institutionId), isNull(), isNull(), isNull(), eq(List.of(productId)), isNull(), isNull());
    }



    @Test
    void getUserInfoV2_shouldBeDeleted() {
        //given
        UserInstitution userInstitution = new UserInstitution();
        userInstitution.setInstitutionId("id");
        userInstitution.setProducts(dummyOnboardedProductResponses());

        OnboardedInstitutionInfo onboardedInstitutionInfo = new OnboardedInstitutionInfo();
        onboardedInstitutionInfo.setId("id");
        ProductInfo productInfo = new ProductInfo();
        productInfo.setId(ProductId.PROD_IO.name());
        productInfo.setStatus(RelationshipState.ACTIVE.name());
        onboardedInstitutionInfo.setProductInfo(productInfo);
        onboardedInstitutionInfo.setState(RelationshipState.ACTIVE.name());
        OnboardedInstitutionResponse onboardedInstitutionResponse = new OnboardedInstitutionResponse();
        onboardedInstitutionResponse.setId("id");
        // when
        when(userMsConnector.searchUserByExternalId(anyString()))
                .thenReturn(dummyUser);
        when(userMsConnector.getUsersInstitutions(anyString(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull())).thenReturn(List.of(userInstitution));
        when(msCoreConnector.getInstitutionDetails(userInstitution.getInstitutionId())).thenReturn(List.of(onboardedInstitutionInfo));

        UserInfoWrapper userWrapper = userService.getUserInfoV2(fiscalCode, null);
        // then
        assertNotNull(userWrapper);
        assertNotNull(userWrapper.getUser());
        assertNotNull(userWrapper.getOnboardedInstitutions());
        assertEquals(dummyUser.getName().getValue(),  userWrapper.getUser().getName().getValue());
        assertEquals(dummyUser.getEmail().getValue(),  userWrapper.getUser().getEmail().getValue());
        assertEquals(RelationshipState.ACTIVE.name(),  userWrapper.getOnboardedInstitutions().get(0).getState());
        assertEquals(RelationshipState.DELETED.name(),  userWrapper.getOnboardedInstitutions().get(0).getProductInfo().getStatus());
    }



    private List<OnboardedProductResponse> dummyOnboardedProductResponses() {
        OnboardedProductResponse onboardedProductActive = new OnboardedProductResponse();
        onboardedProductActive.setProductId(ProductId.PROD_PAGOPA.name());
        onboardedProductActive.setStatus(RelationshipState.ACTIVE.name());
        OnboardedProductResponse onboardedProductPagopaDeleted = new OnboardedProductResponse();
        onboardedProductPagopaDeleted.setProductId(ProductId.PROD_PAGOPA.name());
        onboardedProductPagopaDeleted.setStatus(RelationshipState.DELETED.name());
        OnboardedProductResponse onboardedProductResponse = new OnboardedProductResponse();
        onboardedProductResponse.setProductId(ProductId.PROD_IO.name());
        onboardedProductResponse.setStatus(RelationshipState.DELETED.name());
        OnboardedProductResponse onboardedProductPending = new OnboardedProductResponse();
        onboardedProductPending.setProductId(ProductId.PROD_IO.name());
        onboardedProductPending.setStatus(RelationshipState.PENDING.name());
        return List.of(onboardedProductResponse, onboardedProductPending, onboardedProductActive, onboardedProductPagopaDeleted);
    }

    @Test
    void getUserInfoV2_shouldBeEmptyWhenFilterState() {
        //given
        UserInstitution userInstitution = new UserInstitution();
        userInstitution.setInstitutionId("id");
        userInstitution.setProducts(dummyOnboardedProductResponses());

        OnboardedInstitutionInfo onboardedInstitutionInfo = new OnboardedInstitutionInfo();
        onboardedInstitutionInfo.setId("id");
        ProductInfo productInfo = new ProductInfo();
        productInfo.setId(ProductId.PROD_IO.name());
        productInfo.setStatus(RelationshipState.ACTIVE.name());
        onboardedInstitutionInfo.setProductInfo(productInfo);
        onboardedInstitutionInfo.setState(RelationshipState.ACTIVE.name());
        OnboardedInstitutionResponse onboardedInstitutionResponse = new OnboardedInstitutionResponse();
        onboardedInstitutionResponse.setId("id");
        // when
        when(userMsConnector.searchUserByExternalId(anyString()))
                .thenReturn(dummyUser);
        when(userMsConnector.getUsersInstitutions(anyString(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull())).thenReturn(List.of(userInstitution));
        when(msCoreConnector.getInstitutionDetails(userInstitution.getInstitutionId())).thenReturn(List.of(onboardedInstitutionInfo));

        UserInfoWrapper userWrapper = userService.getUserInfoV2(fiscalCode, List.of(RelationshipState.ACTIVE));
        // then
        assertNotNull(userWrapper);
        assertNotNull(userWrapper.getUser());
        assertNotNull(userWrapper.getOnboardedInstitutions());
        assertEquals(dummyUser.getName().getValue(),  userWrapper.getUser().getName().getValue());
        assertEquals(dummyUser.getEmail().getValue(),  userWrapper.getUser().getEmail().getValue());
        assertTrue(userWrapper.getOnboardedInstitutions().isEmpty());
    }

    @Test
    void getUserInfoV2() {
        //given
        UserInstitution userInstitution = new UserInstitution();
        userInstitution.setInstitutionId("id");
        userInstitution.setProducts(dummyOnboardedProductResponses());
        OnboardedInstitutionInfo onboardedInstitutionInfo = new OnboardedInstitutionInfo();
        onboardedInstitutionInfo.setId("id");
        ProductInfo productInfo = new ProductInfo();
        productInfo.setId(ProductId.PROD_PAGOPA.name());
        productInfo.setStatus(RelationshipState.ACTIVE.name());
        onboardedInstitutionInfo.setProductInfo(productInfo);
        OnboardedInstitutionResponse onboardedInstitutionResponse = new OnboardedInstitutionResponse();
        onboardedInstitutionInfo.setState(RelationshipState.ACTIVE.name());
        onboardedInstitutionResponse.setId("id");
        // when
        when(userMsConnector.searchUserByExternalId(fiscalCode))
                .thenReturn(dummyUser);
        when(userMsConnector.getUsersInstitutions(eq(dummyUser.getId()), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull())).thenReturn(List.of(userInstitution));
        when(msCoreConnector.getInstitutionDetails(userInstitution.getInstitutionId())).thenReturn(List.of(onboardedInstitutionInfo));

        UserInfoWrapper userWrapper = userService.getUserInfoV2(fiscalCode, List.of(RelationshipState.ACTIVE));
        // then
        assertNotNull(userWrapper);
        assertNotNull(userWrapper.getUser());
        assertNotNull(userWrapper.getOnboardedInstitutions());
        assertEquals(dummyUser.getName().getValue(),  userWrapper.getUser().getName().getValue());
        assertEquals(dummyUser.getEmail().getValue(),  userWrapper.getUser().getEmail().getValue());
        assertEquals(RelationshipState.ACTIVE.name(),  userWrapper.getOnboardedInstitutions().get(0).getState());
        assertEquals(RelationshipState.ACTIVE.name(),  userWrapper.getOnboardedInstitutions().get(0).getProductInfo().getStatus());
    }



    @Test
    void getOnboardedInstitutionsDetailsActive(){
        //given
        final String institutionId = "institutionId";
        final String userId = UUID.randomUUID().toString();
        final String productId = "prod-io";
        final String productIdDeleted = "prod-deleted";
        UserInstitution userInstitution = new UserInstitution();
        userInstitution.setInstitutionId(institutionId);

        //Add onboardedProduct Active
        OnboardedProductResponse onboardedProductActive = new OnboardedProductResponse();
        onboardedProductActive.setProductId(productId);
        onboardedProductActive.setStatus(RelationshipState.ACTIVE.name());
        onboardedProductActive.setRole(PartyRole.MANAGER.name());
        onboardedProductActive.setProductRole("admin");
        onboardedProductActive.setCreatedAt(LocalDateTime.now());

        //Add onboardedProduct Deleted
        OnboardedProductResponse onboardedProductDeleted = new OnboardedProductResponse();
        onboardedProductDeleted.setProductId(productIdDeleted);
        onboardedProductDeleted.setStatus(RelationshipState.DELETED.name());

        userInstitution.setProducts(List.of(onboardedProductActive, onboardedProductDeleted));

        //Add to institution both product in state ACTIVE
        OnboardedInstitutionInfo onboardedInstitutionActive = new OnboardedInstitutionInfo();
        onboardedInstitutionActive.setState(RelationshipState.ACTIVE.name());

        ProductInfo productInfoActive = new ProductInfo();
        productInfoActive.setId(productId);
        productInfoActive.setStatus(RelationshipState.ACTIVE.name());
        onboardedInstitutionActive.setProductInfo(productInfoActive);
        OnboardedInstitutionInfo onboardedInstitutionDeleted = new OnboardedInstitutionInfo();

        ProductInfo productInfoDeleted = new ProductInfo();
        productInfoDeleted.setId(productIdDeleted);
        productInfoDeleted.setStatus(RelationshipState.ACTIVE.name());
        onboardedInstitutionDeleted.setProductInfo(productInfoDeleted);

        when(userMsConnector.getUsersInstitutions(anyString(), any(), any(), any(), any(), any(), any(), any())).thenReturn(List.of(userInstitution));
        when(msCoreConnector.getInstitutionDetails(institutionId)).thenReturn(List.of(onboardedInstitutionActive, onboardedInstitutionDeleted));

        //when
        List<OnboardedInstitutionInfo> onboardedInstitutionInfos = userService.getOnboardedInstitutionsDetailsActive(userId, productId);
        //then expect only association ACTIVE
        assertNotNull(onboardedInstitutionInfos);
        assertFalse(onboardedInstitutionInfos.isEmpty());
        assertEquals(1, onboardedInstitutionInfos.size());
        verify(userMsConnector, times(1))
                .getUsersInstitutions(eq(userId), isNull(), isNull(), isNull(), isNull(), eq(List.of(productId)), isNull(), isNull());
    }
}
