package it.pagopa.selfcare.external_api.core;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.external_api.api.MsCoreConnector;
import it.pagopa.selfcare.external_api.api.UserMsConnector;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.institutions.Onboarding;
import it.pagopa.selfcare.external_api.model.onboarding.Billing;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionInfo;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionResource;
import it.pagopa.selfcare.external_api.model.onboarding.mapper.OnboardingInstitutionMapperImpl;
import it.pagopa.selfcare.external_api.model.user.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.nio.file.Files;
import java.util.List;

import static it.pagopa.selfcare.external_api.model.user.RelationshipState.ACTIVE;
import static it.pagopa.selfcare.external_api.model.user.RelationshipState.SUSPENDED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest extends BaseServiceTestUtils {

    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private MsCoreConnector msCoreConnectorMock;
    @Mock
    private UserMsConnector userMsConnectorMock;
    @Spy
    private OnboardingInstitutionMapperImpl onboardingInstitutionMapper;

    @BeforeEach
    public void init() {
        super.setUp();
    }


    @Test
    void getUserOnboardedProductDetailsV2() throws Exception {
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        String institutionId = "123e4567-e89b-12d3-a456-426614174000";
        String productId = "product1";

        ClassPathResource resource = new ClassPathResource("expectations/UserInstitution.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        List<UserInstitution> userInstitutions = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });
        Mockito.when(userMsConnectorMock.getUsersInstitutions(eq(userId), eq(institutionId), any(), any(), any(), eq(List.of(productId)), any(), any())).thenReturn(userInstitutions);

        ClassPathResource expectationResource = new ClassPathResource("expectations/UserDetailsWrapper.json");
        byte[] expectationStream = Files.readAllBytes(expectationResource.getFile().toPath());
        UserDetailsWrapper expectation = objectMapper.readValue(expectationStream, new TypeReference<>() {
        });

        UserDetailsWrapper result = userService.getUserOnboardedProductsDetailsV2(userId, institutionId, productId);
        Assertions.assertEquals(objectMapper.writeValueAsString(expectation), objectMapper.writeValueAsString(result));
    }

    @Test
    void getUserOnboardedProductDetailsV2CheckDateMapping() throws Exception {
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        String institutionId = "123e4567-e89b-12d3-a456-426614174000";
        String productId = "product1";

        ClassPathResource resource = new ClassPathResource("expectations/UserInstitutionWithDate.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        List<UserInstitution> userInstitutions = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });
        Mockito.when(userMsConnectorMock.getUsersInstitutions(eq(userId), eq(institutionId), any(), any(), any(), eq(List.of(productId)), any(), any())).thenReturn(userInstitutions);

        UserDetailsWrapper result = userService.getUserOnboardedProductsDetailsV2(userId, institutionId, productId);
        Assertions.assertNotNull(result.getProductDetails().getCreatedAt());
    }

    @Test
    void getUserOnboardedProductDetailsV2WithoutMatch() throws Exception {
        String userId = "userId";
        String institutionId = "institutionId";
        String productId = "productId";
        ClassPathResource resource = new ClassPathResource("expectations/UserInstitution.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        List<UserInstitution> userInstitutions = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });
        Mockito.when(userMsConnectorMock.getUsersInstitutions(eq(userId), eq(institutionId), any(), any(), any(), eq(List.of(productId)), any(), any())).thenReturn(userInstitutions);
        UserDetailsWrapper result = userService.getUserOnboardedProductsDetailsV2(userId, institutionId, productId);
        Assertions.assertNull(result.getProductDetails());
    }

    @Test
    void getOnboardedInstitutionDetailsActiveEmptyList() throws Exception {
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        String institutionId = "123e4567-e89b-12d3-a456-426614174000";
        String productId = "product1";
        String productIdDeleted = "prod-deleted";

        ClassPathResource resource = new ClassPathResource("expectations/UserInstitution.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        List<UserInstitution> userInstitutions = objectMapper.readValue(resourceStream, new TypeReference<>() {});
        Mockito.when(userMsConnectorMock.getUsersInstitutions(userId, null, null, null, null, List.of(productId), null, List.of(ACTIVE.name())))
                .thenReturn(userInstitutions);

        Onboarding onboardedInstitutionActive = new Onboarding();
        onboardedInstitutionActive.setStatus(SUSPENDED);
        onboardedInstitutionActive.setProductId(productId);
        Onboarding onboardedInstitutionDeleted = new Onboarding();
        onboardedInstitutionDeleted.setProductId(productIdDeleted);
        onboardedInstitutionDeleted.setStatus(RelationshipState.SUSPENDED);
        Institution institution = new Institution();
        institution.setId(institutionId);
        institution.setOnboarding(List.of(onboardedInstitutionActive, onboardedInstitutionDeleted));
        Mockito.when(msCoreConnectorMock.getInstitution(institutionId)).thenReturn(institution);

        List<OnboardedInstitutionResource> result = userService.getOnboardedInstitutionsDetailsActive(userId, productId);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void getOnboardedInstitutionDetailsActive2() throws Exception {
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        String institutionId = "123e4567-e89b-12d3-a456-426614174000";
        String productId = "product1";
        String productIdDeleted = "prod-deleted";
        ClassPathResource resource = new ClassPathResource("expectations/UserInstitution.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        List<UserInstitution> userInstitutions = objectMapper.readValue(resourceStream, new TypeReference<>() {});
        Onboarding onboardedInstitutionActive = new Onboarding();
        onboardedInstitutionActive.setStatus(ACTIVE);
        onboardedInstitutionActive.setProductId(productId);
        Onboarding onboardedInstitutionDeleted = new Onboarding();
        onboardedInstitutionDeleted.setProductId(productIdDeleted);
        onboardedInstitutionDeleted.setStatus(RelationshipState.SUSPENDED);
        Institution institution = new Institution();
        institution.setId(institutionId);
        Billing billing = new Billing();
        billing.setRecipientCode("recipientCode");
        billing.setTaxCodeInvoicing("taxCodeInvoicing");
        onboardedInstitutionActive.setBilling(billing);
        institution.setOnboarding(List.of(onboardedInstitutionActive, onboardedInstitutionDeleted));
        Mockito.when(msCoreConnectorMock.getInstitution(institutionId)).thenReturn(institution);

        Mockito.when(userMsConnectorMock.getUsersInstitutions(userId, null, null, null, null, List.of(productId), null, List.of(ACTIVE.name()))).thenReturn(userInstitutions);
        Mockito.when(msCoreConnectorMock.getInstitution(institutionId)).thenReturn(institution);
        List<OnboardedInstitutionResource> result = userService.getOnboardedInstitutionsDetailsActive(userId, productId);
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(1, result.size());
    }

    @Test
    void getOnboardedInstitutionDetailsActive() throws Exception {
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        String institutionId = "123e4567-e89b-12d3-a456-426614174000";
        String productId = "product1";
        String productIdDeleted = "prod-deleted";
        ClassPathResource resource = new ClassPathResource("expectations/UserInstitution.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        List<UserInstitution> userInstitutions = objectMapper.readValue(resourceStream, new TypeReference<>() {});
        Onboarding onboardedInstitutionActive = new Onboarding();
        onboardedInstitutionActive.setStatus(ACTIVE);
        onboardedInstitutionActive.setProductId(productId);
        Onboarding onboardedInstitutionDeleted = new Onboarding();
        onboardedInstitutionDeleted.setProductId(productIdDeleted);
        onboardedInstitutionDeleted.setStatus(RelationshipState.SUSPENDED);
        Institution institution = new Institution();
        institution.setId(institutionId);
        Billing billing = new Billing();
        billing.setRecipientCode("recipientCode");
        billing.setTaxCodeInvoicing("taxCodeInvoicing");
        institution.setBilling(billing);
        institution.setOnboarding(List.of(onboardedInstitutionActive, onboardedInstitutionDeleted));
        Mockito.when(msCoreConnectorMock.getInstitution(institutionId)).thenReturn(institution);

        Mockito.when(userMsConnectorMock.getUsersInstitutions(userId, null, null, null, null, List.of(productId), null, List.of(ACTIVE.name()))).thenReturn(userInstitutions);
        Mockito.when(msCoreConnectorMock.getInstitution(institutionId)).thenReturn(institution);
        List<OnboardedInstitutionResource> result = userService.getOnboardedInstitutionsDetailsActive(userId, productId);
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(1, result.size());
    }



    @Test
    void getUserInfoV2WithEmptyOnboardedInstitutions() throws Exception {
        String taxCode = "MNCCSD01R13A757G";

        ClassPathResource userInstitutionResource = new ClassPathResource("expectations/UserInstitution.json");
        byte[] userInstitutionStream = Files.readAllBytes(userInstitutionResource.getFile().toPath());
        List<UserInstitution> userInstitution = objectMapper.readValue(userInstitutionStream, new TypeReference<>() {
        });

        ClassPathResource onboardedInstitutionInfoResource = new ClassPathResource("expectations/OnboardedInstitutionInfoV2.json");
        byte[] onboardedInstitutionInfoStream = Files.readAllBytes(onboardedInstitutionInfoResource.getFile().toPath());
        OnboardedInstitutionInfo onboardedInstitutionInfo = objectMapper.readValue(onboardedInstitutionInfoStream, OnboardedInstitutionInfo.class);

        ClassPathResource userResource = new ClassPathResource("expectations/User.json");
        byte[] userStream = Files.readAllBytes(userResource.getFile().toPath());
        User user = objectMapper.readValue(userStream, User.class);
        Mockito.when(userMsConnectorMock.searchUserByExternalId(taxCode)).thenReturn(user);
        Mockito.when(userMsConnectorMock.getUsersInstitutions(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(userInstitution);
        Mockito.when(msCoreConnectorMock.getInstitutionDetails(any())).thenReturn(List.of(onboardedInstitutionInfo));

        UserInfoWrapper userInfoWrapper = userService.getUserInfoV2(taxCode, List.of(ACTIVE));

        Assertions.assertNotNull(userInfoWrapper);
        Assertions.assertNotNull(userInfoWrapper.getUser());
        Assertions.assertEquals(0, userInfoWrapper.getOnboardedInstitutions().size());
        Assertions.assertEquals(user.getName().getValue(), userInfoWrapper.getUser().getName().getValue());
        Assertions.assertEquals(user.getEmail().getValue(), userInfoWrapper.getUser().getEmail().getValue());
    }

    @Test
    void getUserInfoV2WithValidOnboardedInstitutions() throws Exception {
        String taxCode = "MNCCSD01R13A757G";

        ClassPathResource userInstitutionResource = new ClassPathResource("expectations/UserInstitution.json");
        byte[] userInstitutionStream = Files.readAllBytes(userInstitutionResource.getFile().toPath());
        List<UserInstitution> userInstitution = objectMapper.readValue(userInstitutionStream, new TypeReference<>() {
        });

        ClassPathResource onboardedInstitutionInfoResource = new ClassPathResource("expectations/OnboardedInstitutionInfo.json");
        byte[] onboardedInstitutionInfoStream = Files.readAllBytes(onboardedInstitutionInfoResource.getFile().toPath());
        OnboardedInstitutionInfo onboardedInstitutionInfo = objectMapper.readValue(onboardedInstitutionInfoStream, OnboardedInstitutionInfo.class);

        ClassPathResource userResource = new ClassPathResource("expectations/User.json");
        byte[] userStream = Files.readAllBytes(userResource.getFile().toPath());
        User user = objectMapper.readValue(userStream, User.class);
        Mockito.when(userMsConnectorMock.searchUserByExternalId(taxCode)).thenReturn(user);
        Mockito.when(userMsConnectorMock.getUsersInstitutions(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(userInstitution);
        Mockito.when(msCoreConnectorMock.getInstitutionDetails(any())).thenReturn(List.of(onboardedInstitutionInfo));

        UserInfoWrapper userInfoWrapper = userService.getUserInfoV2(taxCode, List.of(ACTIVE));

        ClassPathResource userInfoWrapperResource = new ClassPathResource("expectations/UserInfoWrapperV2.json");
        byte[] userInfoWrapperStream = Files.readAllBytes(userInfoWrapperResource.getFile().toPath());
        UserInfoWrapper expectation = objectMapper.readValue(userInfoWrapperStream, UserInfoWrapper.class);

        Assertions.assertNotNull(userInfoWrapper);
        Assertions.assertNotNull(userInfoWrapper.getUser());
        Assertions.assertEquals(1, userInfoWrapper.getOnboardedInstitutions().size());
        Assertions.assertEquals(objectMapper.writeValueAsString(expectation), objectMapper.writeValueAsString(userInfoWrapper));
    }
}
