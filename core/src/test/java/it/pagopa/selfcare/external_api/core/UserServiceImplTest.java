package it.pagopa.selfcare.external_api.core;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.external_api.api.MsCoreConnector;
import it.pagopa.selfcare.external_api.api.UserMsConnector;
import it.pagopa.selfcare.external_api.api.UserRegistryConnector;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionInfo;
import it.pagopa.selfcare.external_api.model.onboarding.ProductInfo;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest extends BaseServiceTest {

    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private MsCoreConnector msCoreConnectorMock;
    @Mock
    private UserMsConnector userMsConnectorMock;
    @Spy
    private OnboardingInstitutionMapperImpl onboardingInstitutionMapper;

    @BeforeEach
    public void setUp() {
        super.setUp(userService);
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
        Mockito.when(userMsConnectorMock.getUsersInstitutions(userId, null, null, null, null, List.of(productId), null, null)).thenReturn(userInstitutions);

        OnboardedInstitutionInfo onboardedInstitutionActive = new OnboardedInstitutionInfo();
        onboardedInstitutionActive.setState(RelationshipState.SUSPENDED.name());
        ProductInfo productInfoActive = new ProductInfo();
        productInfoActive.setId(productId);
        productInfoActive.setStatus(RelationshipState.SUSPENDED.name());
        onboardedInstitutionActive.setProductInfo(productInfoActive);
        OnboardedInstitutionInfo onboardedInstitutionDeleted = new OnboardedInstitutionInfo();
        ProductInfo productInfoDeleted = new ProductInfo();
        productInfoDeleted.setId(productIdDeleted);
        productInfoDeleted.setStatus(RelationshipState.SUSPENDED.name());
        onboardedInstitutionDeleted.setProductInfo(productInfoDeleted);
        Mockito.when(msCoreConnectorMock.getInstitutionDetails(institutionId)).thenReturn(List.of(onboardedInstitutionActive, onboardedInstitutionDeleted));

        List<OnboardedInstitutionInfo> result = userService.getOnboardedInstitutionsDetailsActive(userId, productId);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
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
        Mockito.when(userMsConnectorMock.getUsersInstitutions(userId, null, null, null, null, List.of(productId), null, null)).thenReturn(userInstitutions);
        Mockito.when(msCoreConnectorMock.getInstitutionDetails(institutionId)).thenReturn(List.of(onboardedInstitutionActive, onboardedInstitutionDeleted));
        List<OnboardedInstitutionInfo> result = userService.getOnboardedInstitutionsDetailsActive(userId, productId);
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

        UserInfoWrapper userInfoWrapper = userService.getUserInfoV2(taxCode, List.of(RelationshipState.ACTIVE));

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

        UserInfoWrapper userInfoWrapper = userService.getUserInfoV2(taxCode, List.of(RelationshipState.ACTIVE));

        ClassPathResource userInfoWrapperResource = new ClassPathResource("expectations/UserInfoWrapperV2.json");
        byte[] userInfoWrapperStream = Files.readAllBytes(userInfoWrapperResource.getFile().toPath());
        UserInfoWrapper expectation = objectMapper.readValue(userInfoWrapperStream, UserInfoWrapper.class);

        Assertions.assertNotNull(userInfoWrapper);
        Assertions.assertNotNull(userInfoWrapper.getUser());
        Assertions.assertEquals(1, userInfoWrapper.getOnboardedInstitutions().size());
        Assertions.assertEquals(objectMapper.writeValueAsString(expectation), objectMapper.writeValueAsString(userInfoWrapper));
    }
}
