package it.pagopa.selfcare.external_api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.external_api.client.MsCoreInstitutionApiClient;
import it.pagopa.selfcare.external_api.client.MsUserApiRestClient;
import it.pagopa.selfcare.external_api.mapper.InstitutionMapper;
import it.pagopa.selfcare.external_api.mapper.UserMapper;
import it.pagopa.selfcare.external_api.model.institution.Institution;
import it.pagopa.selfcare.external_api.model.institution.Onboarding;
import it.pagopa.selfcare.external_api.model.onboarding.Billing;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionInfo;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionResource;
import it.pagopa.selfcare.external_api.model.onboarding.mapper.OnboardingInstitutionMapper;
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
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

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
    @Spy
    private OnboardingInstitutionMapperImpl onboardingInstitutionMapper;
    @Spy
    private InstitutionMapper institutionMapper;

    @Mock
    private  MsCoreInstitutionApiClient institutionApiClient;

    @Spy
    private  UserMapper userMapper;

    @Mock
    private MsUserApiRestClient msUserApiRestClient;


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

        Onboarding onboardedInstitutionActive = new Onboarding();
        onboardedInstitutionActive.setStatus(SUSPENDED);
        onboardedInstitutionActive.setProductId(productId);
        Onboarding onboardedInstitutionDeleted = new Onboarding();
        onboardedInstitutionDeleted.setProductId(productIdDeleted);
        onboardedInstitutionDeleted.setStatus(RelationshipState.SUSPENDED);
        Institution institution = new Institution();
        institution.setId(institutionId);
        institution.setOnboarding(List.of(onboardedInstitutionActive, onboardedInstitutionDeleted));

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
