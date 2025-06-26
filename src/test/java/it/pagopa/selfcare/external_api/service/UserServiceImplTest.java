package it.pagopa.selfcare.external_api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.BillingResponse;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.InstitutionResponse;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.OnboardedProductResponse;
import it.pagopa.selfcare.external_api.TestUtils;
import it.pagopa.selfcare.external_api.client.MsCoreInstitutionApiClient;
import it.pagopa.selfcare.external_api.client.MsUserApiRestClient;
import it.pagopa.selfcare.external_api.mapper.InstitutionMapperImpl;
import it.pagopa.selfcare.external_api.mapper.UserMapperImpl;
import it.pagopa.selfcare.external_api.model.institution.Institution;
import it.pagopa.selfcare.external_api.model.onboarding.Billing;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionInfo;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionResource;
import it.pagopa.selfcare.external_api.model.onboarding.ProductInfo;
import it.pagopa.selfcare.external_api.model.onboarding.mapper.OnboardingInstitutionMapperImpl;
import it.pagopa.selfcare.external_api.model.user.UserDetailsWrapper;
import it.pagopa.selfcare.external_api.model.user.UserInfoWrapper;
import it.pagopa.selfcare.external_api.model.user.UserInstitution;
import it.pagopa.selfcare.onboarding.common.PartyRole;
import it.pagopa.selfcare.product.service.ProductService;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.CertifiableFieldResponseString;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.UserDetailResponse;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.UserInstitutionResponse;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.WorkContactResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;

import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static it.pagopa.selfcare.external_api.model.user.RelationshipState.ACTIVE;
import static it.pagopa.selfcare.external_api.model.user.RelationshipState.SUSPENDED;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest extends BaseServiceTestUtils {

    @Spy
    @InjectMocks
    private UserServiceImpl userService;
    @Spy
    private OnboardingInstitutionMapperImpl onboardingInstitutionMapper;
    @Spy
    private InstitutionMapperImpl institutionMapper;

    @Mock
    private  MsCoreInstitutionApiClient institutionApiClient;

    @Spy
    private UserMapperImpl userMapper;

    @Mock
    private MsUserApiRestClient msUserApiRestClient;

    @Mock
    private ProductService productService;


    static final String PRODUCT_ID = "product1";


    @BeforeEach
    public void init() {
        super.setUp();
    }


    @Test
    void getUserOnboardedProductDetailsV2() throws Exception {
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        String institutionId = "123e4567-e89b-12d3-a456-426614174000";

        ClassPathResource resource = new ClassPathResource("expectations/UserInstitution.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        List<UserInstitutionResponse> userInstitutions = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });
        Mockito.when(msUserApiRestClient._retrievePaginatedAndFilteredUser(institutionId, null, null, List.of(PRODUCT_ID), null, null, null,userId))
                .thenReturn(ResponseEntity.ok(userInstitutions));
        Mockito.when(userMapper.getProductService()).thenReturn(productService);
        Mockito.when(productService.getProductRaw(any())).thenReturn(TestUtils.dummyProduct(PRODUCT_ID));


        ClassPathResource expectationResource = new ClassPathResource("expectations/UserDetailsWrapper.json");
        byte[] expectationStream = Files.readAllBytes(expectationResource.getFile().toPath());
        UserDetailsWrapper expectation = objectMapper.readValue(expectationStream, new TypeReference<>() {
        });

        UserDetailsWrapper result = userService.getUserOnboardedProductsDetailsV2(userId, institutionId, PRODUCT_ID);
        Assertions.assertEquals(objectMapper.writeValueAsString(expectation), objectMapper.writeValueAsString(result));
    }

    @Test
    void getUserOnboardedProductDetailsV2CheckDateMapping() throws Exception {
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        String institutionId = "123e4567-e89b-12d3-a456-426614174000";

        ClassPathResource resource = new ClassPathResource("expectations/UserInstitutionWithDate.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        List<UserInstitutionResponse> userInstitutions = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });
        Mockito.when(msUserApiRestClient._retrievePaginatedAndFilteredUser(institutionId, null, null, List.of(PRODUCT_ID), null, null, null,userId))
                .thenReturn(ResponseEntity.ok(userInstitutions));
        Mockito.when(userMapper.getProductService()).thenReturn(productService);
        Mockito.when(productService.getProductRaw(any())).thenReturn(TestUtils.dummyProduct(PRODUCT_ID));

        UserDetailsWrapper result = userService.getUserOnboardedProductsDetailsV2(userId, institutionId, PRODUCT_ID);
        Assertions.assertNotNull(result.getProductDetails().getCreatedAt());
    }

    @Test
    void getUserOnboardedProductDetailsV2WithoutMatch() throws Exception {
        String userId = "userId";
        String institutionId = "institutionId";
        ClassPathResource resource = new ClassPathResource("expectations/UserInstitution.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        List<UserInstitutionResponse> userInstitutions = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });
        Mockito.when(msUserApiRestClient._retrievePaginatedAndFilteredUser(institutionId, null, null, List.of(PRODUCT_ID), null, null, null,userId))
                .thenReturn(ResponseEntity.ok(userInstitutions));
        Mockito.when(userMapper.getProductService()).thenReturn(productService);
        Mockito.when(productService.getProductRaw(any())).thenReturn(TestUtils.dummyProduct(PRODUCT_ID));

        UserDetailsWrapper result = userService.getUserOnboardedProductsDetailsV2(userId, institutionId, PRODUCT_ID);
        Assertions.assertNull(result.getProductDetails());
    }

    @Test
    void getOnboardedInstitutionDetailsActiveEmptyList() throws Exception {
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        String institutionId = "123e4567-e89b-12d3-a456-426614174000";
        String productIdDeleted = "prod-deleted";

        ClassPathResource resource = new ClassPathResource("expectations/UserInstitution.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        List<UserInstitutionResponse> userInstitutions = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });
        Mockito.when(msUserApiRestClient._retrievePaginatedAndFilteredUser(null, null, null, List.of(PRODUCT_ID), null, null, List.of(ACTIVE.name()),userId))
                .thenReturn(ResponseEntity.ok(userInstitutions));
        Mockito.when(userMapper.getProductService()).thenReturn(productService);
        Mockito.when(productService.getProductRaw(any())).thenReturn(TestUtils.dummyProduct(PRODUCT_ID));

        InstitutionResponse institution = getInstitutionResponse(PRODUCT_ID, productIdDeleted, institutionId, false);
        institution.getOnboarding().forEach(onboardedProductResponse -> onboardedProductResponse.setStatus(OnboardedProductResponse.StatusEnum.SUSPENDED));
        Mockito.when(institutionApiClient._retrieveInstitutionByIdUsingGET(institutionId)).thenReturn(ResponseEntity.ok(institution));


        List<OnboardedInstitutionResource> result = userService.getOnboardedInstitutionsDetailsActive(userId, PRODUCT_ID);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
            "true, true",
            "false, false",
            "true, false",
            "false, true"
    })
    void getOnboardedInstitutionDetailsActive(boolean withBillingOnboarding, boolean withBillingInstitution) throws Exception {
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        String institutionId = "123e4567-e89b-12d3-a456-426614174000";
        String productIdDeleted = "prod-deleted";
        ClassPathResource resource = new ClassPathResource("expectations/UserInstitution.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        List<UserInstitutionResponse> userInstitutions = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });
        Mockito.when(msUserApiRestClient._retrievePaginatedAndFilteredUser(null, null, null, List.of(PRODUCT_ID), null, null, List.of(ACTIVE.name()),userId))
                .thenReturn(ResponseEntity.ok(userInstitutions));

        InstitutionResponse institution = getInstitutionResponse(PRODUCT_ID, productIdDeleted, institutionId, withBillingOnboarding);
        if(withBillingInstitution) {
            Institution institutionMapped = institutionMapper.toInstitution(institution);
            Billing billing = new Billing();
            billing.setVatNumber("123");
            billing.setRecipientCode("123");
            institutionMapped.setBilling(billing);
        Mockito.when(institutionMapper.toInstitution(any())).thenReturn(institutionMapped);
        }
        Mockito.when(institutionApiClient._retrieveInstitutionByIdUsingGET(institutionId)).thenReturn(ResponseEntity.ok(institution));
        Mockito.when(userMapper.getProductService()).thenReturn(productService);
        Mockito.when(productService.getProductRaw(any())).thenReturn(TestUtils.dummyProduct(PRODUCT_ID));

        List<OnboardedInstitutionResource> result = userService.getOnboardedInstitutionsDetailsActive(userId, PRODUCT_ID);
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(1, result.size());
    }

    private static InstitutionResponse getInstitutionResponse(String productId, String productIdDeleted, String institutionId, boolean withBilling) {
        it.pagopa.selfcare.core.generated.openapi.v1.dto.OnboardedProductResponse onboardedInstitutionActive = new it.pagopa.selfcare.core.generated.openapi.v1.dto.OnboardedProductResponse();
        onboardedInstitutionActive.setStatus(it.pagopa.selfcare.core.generated.openapi.v1.dto.OnboardedProductResponse.StatusEnum.ACTIVE);
        onboardedInstitutionActive.setProductId(productId);
        it.pagopa.selfcare.core.generated.openapi.v1.dto.OnboardedProductResponse onboardedInstitutionDeleted = new it.pagopa.selfcare.core.generated.openapi.v1.dto.OnboardedProductResponse ();
        onboardedInstitutionDeleted.setProductId(productIdDeleted);
        onboardedInstitutionDeleted.setStatus(OnboardedProductResponse.StatusEnum.SUSPENDED);
        InstitutionResponse institution = new InstitutionResponse();
        institution.setId(institutionId);
        if(withBilling) {
            BillingResponse billing = new BillingResponse();
            billing.setRecipientCode("recipientCode");
            billing.setTaxCodeInvoicing("taxCodeInvoicing");
            onboardedInstitutionActive.setBilling(billing);
        }
        institution.setOnboarding(List.of(onboardedInstitutionActive, onboardedInstitutionDeleted));
        return institution;
    }


    @Test
    void getUserInfoV2WithEmptyOnboardedInstitutions() throws Exception {
        String taxCode = "MNCCSD01R13A757G";

        ClassPathResource userResource = new ClassPathResource("expectations/User.json");
        byte[] userStream = Files.readAllBytes(userResource.getFile().toPath());
        UserDetailResponse user = objectMapper.readValue(userStream, UserDetailResponse.class);
        Mockito.when(msUserApiRestClient._searchUserByFiscalCode(any(), any())).thenReturn(ResponseEntity.ok(user));

        ClassPathResource userInstitutionResource = new ClassPathResource("expectations/UserInstitution.json");
        byte[] userInstitutionStream = Files.readAllBytes(userInstitutionResource.getFile().toPath());
        List<UserInstitutionResponse> userInstitutions = objectMapper.readValue(userInstitutionStream, new TypeReference<>() {
        });

        Mockito.when(msUserApiRestClient._retrievePaginatedAndFilteredUser(null, null, null, null, null, 350, null,user.getId()))
                .thenReturn(ResponseEntity.ok(userInstitutions));
        Mockito.when(userMapper.getProductService()).thenReturn(productService);
        Mockito.when(productService.getProductRaw(any())).thenReturn(TestUtils.dummyProduct(PRODUCT_ID));

        UserInfoWrapper userInfoWrapper = userService.getUserInfoV2(taxCode, List.of(ACTIVE));

        Assertions.assertNotNull(userInfoWrapper);
        Assertions.assertNotNull(userInfoWrapper.getUser());
        Assertions.assertEquals(0, userInfoWrapper.getOnboardedInstitutions().size());
        Assertions.assertEquals(user.getName().getValue(), userInfoWrapper.getUser().getName().getValue());
        Assertions.assertEquals(user.getEmail().getValue(), userInfoWrapper.getUser().getEmail().getValue());
    }

    @ParameterizedTest
    @CsvSource({
            "expectations/UserInstitutionWithCreatedAt.json, expectations/UserInfoWrapperV2.json",
            "expectations/UserInstitutionWithCreatedAtTwoOnboardingsPerProduct.json, expectations/UserInfoWrapperV2TwoOnboardingsPerProduct.json",
            "expectations/UserInstitutionWithCreatedAtTwoOnboardingsPerProductInverted.json, expectations/UserInfoWrapperV2TwoOnboardingsPerProduct.json"
    })
    void getUserInfoV2WithValidOnboardedInstitutions(String userInstitutionPath, String userInfoWrapperPath) throws Exception {
        String taxCode = "MNCCSD01R13A757G";

        ClassPathResource userResource = new ClassPathResource("expectations/User.json");
        byte[] userStream = Files.readAllBytes(userResource.getFile().toPath());
        UserDetailResponse user = objectMapper.readValue(userStream, UserDetailResponse.class);
        Mockito.when(msUserApiRestClient._searchUserByFiscalCode(any(), any())).thenReturn(ResponseEntity.ok(user));

        ClassPathResource userInstitutionResource = new ClassPathResource(userInstitutionPath);
        byte[] userInstitutionStream = Files.readAllBytes(userInstitutionResource.getFile().toPath());
        List<UserInstitutionResponse> userInstitutions = objectMapper.readValue(userInstitutionStream, new TypeReference<>() {
        });
        Mockito.when(msUserApiRestClient._retrievePaginatedAndFilteredUser(null, null, null, null, null, 350, null,user.getId()))
                .thenReturn(ResponseEntity.ok(userInstitutions));
        Mockito.when(userMapper.getProductService()).thenReturn(productService);
        Mockito.when(productService.getProductRaw(any())).thenReturn(TestUtils.dummyProduct(PRODUCT_ID));

        InstitutionResponse institution = getInstitutionResponse("product1", "product2", "123e4567-e89b-12d3-a456-426614174000", false);
        Mockito.when(institutionApiClient._retrieveInstitutionByIdUsingGET("123e4567-e89b-12d3-a456-426614174000")).thenReturn(ResponseEntity.ok(institution));

        UserInfoWrapper userInfoWrapper = userService.getUserInfoV2(taxCode, List.of(ACTIVE));

        ClassPathResource userInfoWrapperResource = new ClassPathResource(userInfoWrapperPath);
        byte[] userInfoWrapperStream = Files.readAllBytes(userInfoWrapperResource.getFile().toPath());
        UserInfoWrapper expectation = objectMapper.readValue(userInfoWrapperStream, UserInfoWrapper.class);


        Assertions.assertNotNull(userInfoWrapper);
        Assertions.assertNotNull(userInfoWrapper.getUser());
        Assertions.assertEquals(2, userInfoWrapper.getOnboardedInstitutions().size());
        Assertions.assertEquals(objectMapper.writeValueAsString(expectation.getUser()), objectMapper.writeValueAsString(userInfoWrapper.getUser()));
        Assertions.assertEquals(
                objectMapper.writeValueAsString(
                        expectation.getOnboardedInstitutions()
                                .stream()
                                .peek(onboardedInstitution -> onboardedInstitution.getProductInfo().setCreatedAt(null))
                                .sorted(Comparator.comparing(inst -> inst.getProductInfo().getId()))
                            .toList()
                ),
                objectMapper.writeValueAsString(userInfoWrapper.getOnboardedInstitutions()
                        .stream()
                        .peek(onboardedInstitution -> onboardedInstitution.getProductInfo().setCreatedAt(null))
                        .sorted(Comparator.comparing(inst -> inst.getProductInfo().getId()))
                    .toList()
                )
        );
    }

    @Test
    void getUserInfoV2WithLastOnboardingUserEmail() {
        final UserDetailResponse user = new UserDetailResponse();
        user.setId("userId");
        final CertifiableFieldResponseString email1 = new CertifiableFieldResponseString();
        email1.setValue("test1@test.com");
        final CertifiableFieldResponseString email2 = new CertifiableFieldResponseString();
        email2.setValue("test2@test.com");
        final CertifiableFieldResponseString email3 = new CertifiableFieldResponseString();
        email3.setValue("test3@test.com");
        final CertifiableFieldResponseString email4 = new CertifiableFieldResponseString();
        email4.setValue("test4@test.com");
        final CertifiableFieldResponseString email5 = new CertifiableFieldResponseString();
        email4.setValue("test5@test.com");
        user.setWorkContacts(Map.of(
            "1", new WorkContactResponse().email(email1),
            "2", new WorkContactResponse().email(email2),
            "3", new WorkContactResponse().email(email3),
            "4", new WorkContactResponse().email(email4),
            "5", new WorkContactResponse().email(email5)
        ));
        user.setEmail(email1);
        when(msUserApiRestClient._searchUserByFiscalCode(any(), any())).thenReturn(ResponseEntity.ok(user));

        final List<OnboardedInstitutionInfo> onboardedInstitutionInfos = List.of(
            createOnboardedInstitutionInfo("info1", "prod-1", "ACTIVE", "ACTIVE", OffsetDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), "1"),
            createOnboardedInstitutionInfo("info2", "prod-2", "ACTIVE", "ACTIVE", OffsetDateTime.of(2025, 1, 2, 1, 0, 0, 0, ZoneOffset.UTC), "2"),
            createOnboardedInstitutionInfo("info3", "prod-3", "ACTIVE", "SUSPENDED", OffsetDateTime.of(2025, 2, 2, 0, 0, 0, 0, ZoneOffset.UTC), "3"),
            createOnboardedInstitutionInfo("info4", "prod-4", "ACTIVE", "ACTIVE", OffsetDateTime.of(2025, 1, 2, 0, 0, 0, 0, ZoneOffset.UTC), "4"),
            createOnboardedInstitutionInfo("info5", "prod-5", "SUSPENDED", "ACTIVE", OffsetDateTime.of(2025, 6, 1, 0, 0, 0, 0, ZoneOffset.UTC), "5")
        );
        doReturn(onboardedInstitutionInfos).when(userService).getOnboardedInstitutionsDetails("userId", null);

        Assertions.assertEquals("test3@test.com", userService.getUserInfoV2("TEST", List.of()).getUser().getLastActiveOnboardingUserEmail());
        Assertions.assertEquals("test2@test.com", userService.getUserInfoV2("TEST", List.of(ACTIVE)).getUser().getLastActiveOnboardingUserEmail());
        Assertions.assertEquals("test3@test.com", userService.getUserInfoV2("TEST", List.of(SUSPENDED)).getUser().getLastActiveOnboardingUserEmail());
    }

    private OnboardedInstitutionInfo createOnboardedInstitutionInfo(String id, String prodId, String state, String status, OffsetDateTime createdAt, String userMailUuid) {
        final OnboardedInstitutionInfo info = new OnboardedInstitutionInfo();
        info.setId(id);
        info.setState(state);
        final ProductInfo productInfo = new ProductInfo();
        productInfo.setId(prodId);
        productInfo.setStatus(status);
        productInfo.setCreatedAt(createdAt);
        info.setProductInfo(productInfo);
        info.setUserMailUuid(userMailUuid);
        return info;
    }

    /**
     * Utility method to create a dummy UserInstitutionResponse.
     */
    private UserInstitutionResponse createUserInstitutionResponse(String id, String institutionId) {
        UserInstitutionResponse response = new UserInstitutionResponse();
        response.setId(id);
        response.setInstitutionId(institutionId);
        // Set other necessary fields as required
        return response;
    }

    /**
     * Test case for successful retrieval of user institutions.
     * Verifies that the method returns a mapped list of UserInstitution objects when the API returns valid responses.
     */
    @Test
    void testGetUsersInstitutions_Success() {
        // Input data
        String userId = "user123";
        String institutionId = "inst456";
        Integer page = 1;
        Integer size = 10;
        List<String> productRoles = Arrays.asList("role1", "role2");
        List<String> products = Arrays.asList("prod1", "prod2");
        List<PartyRole> roles = Arrays.asList(PartyRole.MANAGER, PartyRole.OPERATOR);
        List<String> states = Arrays.asList("active", "pending");

        // Create dummy UserInstitutionResponse objects
        UserInstitutionResponse response1 = createUserInstitutionResponse("resp1", institutionId);
        UserInstitutionResponse response2 = createUserInstitutionResponse("resp2", institutionId);
        List<UserInstitutionResponse> responseList = Arrays.asList(response1, response2);

        // Mock the API client's _retrievePaginatedAndFilteredUser method to return the dummy responses
        ResponseEntity<List<UserInstitutionResponse>> mockResponseEntity = ResponseEntity.ok(responseList);
        when(msUserApiRestClient._retrievePaginatedAndFilteredUser(
                eq(institutionId),
                eq(page),
                eq(productRoles),
                eq(products),
                any(), // Adjust if you want to verify specific PartyRole DTOs
                eq(size),
                eq(states),
                eq(userId)
        )).thenReturn(mockResponseEntity);

        // Execute the method under test
        List<UserInstitution> result = userService.getUsersInstitutions(userId, institutionId, page, size, productRoles, products, roles, states);

        // Assertions to verify the result
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);

        // Verify interactions with mocks
        verify(msUserApiRestClient, times(1))._retrievePaginatedAndFilteredUser(
                eq(institutionId),
                eq(page),
                eq(productRoles),
                eq(products),
                any(),
                eq(size),
                eq(states),
                eq(userId)
        );
    }

    /**
     * Test case for handling an empty response from the API.
     * Verifies that the method returns an empty list when the API returns no UserInstitutionResponse objects.
     */
    @Test
    void testGetUsersInstitutions_EmptyResponse() {
        // Input data
        String userId = "user123";
        String institutionId = "inst456";
        Integer page = 1;
        Integer size = 10;
        List<String> productRoles = Arrays.asList("role1", "role2");
        List<String> products = Arrays.asList("prod1", "prod2");
        List<PartyRole> roles = Arrays.asList(PartyRole.MANAGER, PartyRole.OPERATOR);
        List<String> states = Arrays.asList("active", "pending");

        // Mock the API client's _retrievePaginatedAndFilteredUser method to return an empty list
        ResponseEntity<List<UserInstitutionResponse>> mockResponseEntity = ResponseEntity.ok(Collections.emptyList());
        when(msUserApiRestClient._retrievePaginatedAndFilteredUser(
                eq(institutionId),
                eq(page),
                eq(productRoles),
                eq(products),
                any(),
                eq(size),
                eq(states),
                eq(userId)
        )).thenReturn(mockResponseEntity);

        // Execute the method under test
        List<UserInstitution> result = userService.getUsersInstitutions(userId, institutionId, page, size, productRoles, products, roles, states);

        // Assertions to verify the result
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        // Verify interactions with mocks
        verify(msUserApiRestClient, times(1))._retrievePaginatedAndFilteredUser(
                eq(institutionId),
                eq(page),
                eq(productRoles),
                eq(products),
                any(),
                eq(size),
                eq(states),
                eq(userId)
        );
        verify(userMapper, never()).toUserInstitutionsFromUserInstitutionResponse(any());
    }

    /**
     * Test case for handling a null response body from the API.
     * Verifies that the method throws a NullPointerException when the API returns a null body.
     */
    @Test
    void testGetUsersInstitutions_NullResponseBody() {
        // Input data
        String userId = "user123";
        String institutionId = "inst456";
        Integer page = 1;
        Integer size = 10;
        List<String> productRoles = Arrays.asList("role1", "role2");
        List<String> products = Arrays.asList("prod1", "prod2");
        List<PartyRole> roles = Arrays.asList(PartyRole.MANAGER, PartyRole.OPERATOR);
        List<String> states = Arrays.asList("active", "pending");

        // Mock the API client's _retrievePaginatedAndFilteredUser method to return a response with null body
        ResponseEntity<List<UserInstitutionResponse>> mockResponseEntity = ResponseEntity.ok(null);
        when(msUserApiRestClient._retrievePaginatedAndFilteredUser(
                eq(institutionId),
                eq(page),
                eq(productRoles),
                eq(products),
                any(),
                eq(size),
                eq(states),
                eq(userId)
        )).thenReturn(mockResponseEntity);

        // Execute the method under test and verify that a NullPointerException is thrown
        assertThatThrownBy(() -> userService.getUsersInstitutions(userId, institutionId, page, size, productRoles, products, roles, states))
                .isInstanceOf(NullPointerException.class);

        // Verify interactions with mocks
        verify(msUserApiRestClient, times(1))._retrievePaginatedAndFilteredUser(
                eq(institutionId),
                eq(page),
                eq(productRoles),
                eq(products),
                any(),
                eq(size),
                eq(states),
                eq(userId)
        );
        verify(userMapper, never()).toUserInstitutionsFromUserInstitutionResponse(any());
    }

    /**
     * Test case for handling exceptions thrown by the API client.
     * Verifies that the method propagates exceptions thrown by the API client.
     */
    @Test
    void testGetUsersInstitutions_ApiThrowsException() {
        // Input data
        String userId = "user123";
        String institutionId = "inst456";
        Integer page = 1;
        Integer size = 10;
        List<String> productRoles = Arrays.asList("role1", "role2");
        List<String> products = Arrays.asList("prod1", "prod2");
        List<PartyRole> roles = Arrays.asList(PartyRole.MANAGER, PartyRole.OPERATOR);
        List<String> states = Arrays.asList("active", "pending");

        // Mock the API client's _retrievePaginatedAndFilteredUser method to throw a RuntimeException
        when(msUserApiRestClient._retrievePaginatedAndFilteredUser(
                eq(institutionId),
                eq(page),
                eq(productRoles),
                eq(products),
                any(),
                eq(size),
                eq(states),
                eq(userId)
        )).thenThrow(new RuntimeException("API error"));

        // Execute the method under test and verify that the exception is propagated
        assertThatThrownBy(() -> userService.getUsersInstitutions(userId, institutionId, page, size, productRoles, products, roles, states))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("API error");

        // Verify interactions with mocks
        verify(msUserApiRestClient, times(1))._retrievePaginatedAndFilteredUser(
                eq(institutionId),
                eq(page),
                eq(productRoles),
                eq(products),
                any(),
                eq(size),
                eq(states),
                eq(userId)
        );
        verify(userMapper, never()).toUserInstitutionsFromUserInstitutionResponse(any());
    }

}
