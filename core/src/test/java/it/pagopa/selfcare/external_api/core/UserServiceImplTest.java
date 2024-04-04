package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.external_api.api.MsCoreConnector;
import it.pagopa.selfcare.external_api.api.UserMsConnector;
import it.pagopa.selfcare.external_api.api.UserRegistryConnector;
import it.pagopa.selfcare.external_api.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionInfo;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionResponse;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingInfoResponse;
import it.pagopa.selfcare.external_api.model.onboarding.ProductInfo;
import it.pagopa.selfcare.external_api.model.onboarding.mapper.OnboardingInstitutionMapper;
import it.pagopa.selfcare.external_api.model.user.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.test.context.TestSecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private OnboardingInstitutionMapper onboardingInstitutionMapper;

    @Mock
    private MsCoreConnector msCoreConnector;

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
    void getUserInfoV2() {
        //given
        UserInstitution userInstitution = new UserInstitution();
        userInstitution.setInstitutionId("id");
        OnboardedProductResponse onboardedProductResponse = new OnboardedProductResponse();
        onboardedProductResponse.setProductId("prod-io");
        onboardedProductResponse.setStatus("ACTIVE");
        userInstitution.setProducts(List.of(onboardedProductResponse));
        OnboardedInstitutionInfo onboardedInstitutionInfo = new OnboardedInstitutionInfo();
        onboardedInstitutionInfo.setId("id");
        ProductInfo productInfo = new ProductInfo();
        productInfo.setId("prod-io");
        onboardedInstitutionInfo.setProductInfo(productInfo);
        OnboardedInstitutionResponse onboardedInstitutionResponse = new OnboardedInstitutionResponse();
        onboardedInstitutionResponse.setId("id");
        // when
        when(userMsConnector.searchUserByExternalId(anyString()))
                .thenReturn(dummyUser);
        when(userMsConnector.getUsersInstitutions(anyString())).thenReturn(List.of(userInstitution));
        when(msCoreConnector.getInstitutionDetails(anyString())).thenReturn(List.of(onboardedInstitutionInfo));

        UserInfoWrapper userWrapper = userService.getUserInfoV2(fiscalCode, List.of(RelationshipState.ACTIVE));
        // then
        assertNotNull(userWrapper);
        assertNotNull(userWrapper.getUser());
        assertNotNull(userWrapper.getOnboardedInstitutions());
        assertEquals(dummyUser.getName().getValue(),  userWrapper.getUser().getName().getValue());
        assertEquals(dummyUser.getEmail().getValue(),  userWrapper.getUser().getEmail().getValue());
    }
}
