package it.pagopa.selfcare.external_api.connector.rest.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.commons.connector.rest.BaseFeignRestClientTest;
import it.pagopa.selfcare.commons.connector.rest.RestTestUtils;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.external_api.connector.rest.config.PartyProcessRestClientTestConfig;
import it.pagopa.selfcare.external_api.connector.rest.model.institution.OnBoardingInfo;
import it.pagopa.selfcare.external_api.connector.rest.model.institution.RelationshipsResponse;
import it.pagopa.selfcare.external_api.connector.rest.model.onboarding.OnboardingImportInstitutionRequest;
import it.pagopa.selfcare.external_api.connector.rest.model.onboarding.InstitutionSeed;
import it.pagopa.selfcare.external_api.connector.rest.model.onboarding.OnboardingImportInstitutionRequest;
import it.pagopa.selfcare.external_api.exception.ResourceNotFoundException;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.onboarding.User;
import it.pagopa.selfcare.external_api.model.user.RelationshipState;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.commons.httpclient.HttpClientConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.TestPropertySourceUtils;

import java.util.*;

import static it.pagopa.selfcare.commons.base.security.PartyRole.MANAGER;
import static it.pagopa.selfcare.commons.base.security.PartyRole.OPERATOR;
import static it.pagopa.selfcare.commons.utils.TestUtils.checkNotNullFields;
import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.external_api.model.user.RelationshipState.ACTIVE;
import static it.pagopa.selfcare.external_api.model.user.RelationshipState.PENDING;
import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource(
        locations = "classpath:config/party-process-rest-client.properties",
        properties = {
                "logging.level.it.pagopa.selfcare.external_api.connector.rest=DEBUG",
                "spring.application.name=selc-external-api-connector-rest",
                "feign.okhttp.enabled=true"
        })
@ContextConfiguration(
        initializers = PartyProcessRestClientTest.RandomPortInitializer.class,
        classes = {PartyProcessRestClientTestConfig.class, HttpClientConfiguration.class})
class PartyProcessRestClientTest extends BaseFeignRestClientTest {

    @Order(1)
    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(RestTestUtils.getWireMockConfiguration("stubs/party-process"))
            .build();


    public static class RandomPortInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @SneakyThrows
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                    String.format("USERVICE_PARTY_PROCESS_URL=%s/pdnd-interop-uservice-party-process/0.0.1",
                            wm.getRuntimeInfo().getHttpBaseUrl())
            );
        }
    }

    private enum TestCase {
        FULLY_VALUED,
        FULLY_NULL,
        EMPTY_RESULT
    }

    private static final Map<TestCase, String> testCase2instIdMap = new EnumMap<>(TestCase.class) {{
        put(TestCase.FULLY_VALUED, "institutionId1");
        put(TestCase.FULLY_NULL, "institutionId2");
        put(TestCase.EMPTY_RESULT, "institutionId3");
    }};

    @Autowired
    private PartyProcessRestClient restClient;
    @Test
    void getInstitutionRelationships_fullyValued() {
        // given
        String externalId = testCase2instIdMap.get(TestCase.FULLY_VALUED);
        EnumSet<PartyRole> roles = null;
        EnumSet<RelationshipState> states = null;
        Set<String> products = null;
        Set<String> productRole = null;
        String userId = null;
        // when
        RelationshipsResponse response = restClient.getUserInstitutionRelationships(externalId, roles, states, products, productRole, userId);
        // then
        assertNotNull(response);
        assertFalse(response.isEmpty());
        response.forEach(relationshipInfo -> {
            TestUtils.checkNotNullFields(relationshipInfo);
            TestUtils.checkNotNullFields(relationshipInfo.getInstitutionUpdate());
            TestUtils.checkNotNullFields(relationshipInfo.getBilling());
        });
    }


    @Test
    void getInstitutionRelationships_fullyNull() {
        // given
        String externalId = testCase2instIdMap.get(TestCase.FULLY_NULL);
        EnumSet<PartyRole> roles = null;
        EnumSet<RelationshipState> states = null;
        Set<String> products = null;
        Set<String> productRole = null;
        String userId = null;
        // when
        RelationshipsResponse response = restClient.getUserInstitutionRelationships(externalId, roles, states, products, productRole, userId);
        // then
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertNull(response.get(0).getId());
        assertNull(response.get(0).getFrom());
        assertNull(response.get(0).getRole());
        assertNull(response.get(0).getProduct());
        assertNull(response.get(0).getState());
    }


    @Test
    void getInstitutionRelationships_emptyResult() {
        // given
        String externalId = testCase2instIdMap.get(TestCase.EMPTY_RESULT);
        EnumSet<PartyRole> roles = EnumSet.of(MANAGER, OPERATOR);
        EnumSet<RelationshipState> states = EnumSet.of(ACTIVE, PENDING);
        Set<String> products = Set.of("prod1", "prod2");
        Set<String> productRole = Set.of("api", "security");
        String userId = "userId";
        // when
        RelationshipsResponse response = restClient.getUserInstitutionRelationships(externalId, roles, states, products, productRole, userId);

        // then
        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    void getOnBoardingInfo_fullyValued() {
        // given and when
        OnBoardingInfo response = restClient.getOnBoardingInfo(testCase2instIdMap.get(TestCase.FULLY_VALUED), null);
        // then
        assertNotNull(response);
        response.getInstitutions().forEach(onboardingResponseData -> {
            TestUtils.checkNotNullFields(onboardingResponseData);
            TestUtils.checkNotNullFields(onboardingResponseData.getBilling());
            TestUtils.checkNotNullFields(onboardingResponseData.getProductInfo());
        });

    }


    @Test
    void getOnBoardingInfo_fullyNull() {
        // given and when
        OnBoardingInfo response = restClient.getOnBoardingInfo(testCase2instIdMap.get(TestCase.FULLY_NULL), EnumSet.of(ACTIVE));
        // then
        assertNotNull(response);
        assertNull(response.getUserId());
        assertNull(response.getInstitutions());
    }


    @Test
    void getOnBoardingInfo_emptyResult() {
        // given and when
        OnBoardingInfo response = restClient.getOnBoardingInfo(testCase2instIdMap.get(TestCase.EMPTY_RESULT), EnumSet.of(ACTIVE, PENDING));
        // then
        assertNotNull(response);
        assertTrue(response.getInstitutions().isEmpty());
        assertNull(response.getUserId());
    }

    @Test
    void verifyOnboarding_found() {
        // given
        String externalId = "externalId";
        final String productId = "productId";
        // when
        Executable executable = () -> restClient.verifyOnboarding(externalId, productId);
        //then
        assertDoesNotThrow(executable);
    }


    @Test
    void verifyOnboarding_notFound() {
        //given
        String externalId = "externalIdNotFound";
        final String productId = "productId";
        //when
        Executable executable = () -> restClient.verifyOnboarding(externalId, productId);
        //then
        assertThrows(ResourceNotFoundException.class, executable);
    }


    @Test
    void getInstitutionByExternalId_fullyValued() {
        //given
        String externalId = testCase2instIdMap.get(TestCase.FULLY_VALUED);
        //when
        Institution response = restClient.getInstitutionByExternalId(externalId);
        //then
        assertNotNull(response);
        assertNotNull(response.getId());
        assertNotNull(response.getAddress());
    }

    @Test
    void getInstitutionByExternalId_fullyNull() {
        // given
        String externalId = testCase2instIdMap.get(TestCase.FULLY_NULL);
        // when
        Institution response = restClient.getInstitutionByExternalId(externalId);
        //then
        assertNotNull(response);
        assertNull(response.getAddress());
        assertNull(response.getDescription());
        assertNull(response.getDigitalAddress());
        assertNull(response.getId());
        assertNull(response.getExternalId());
        assertNull(response.getTaxCode());
        assertNull(response.getZipCode());
        assertNull(response.getOrigin());
        assertNull(response.getAttributes());
    }


    @Test
    void getInstitutionByExternalId_notFound() {
        //given
        String externalId = "externalIdNotFound";
        //when
        Executable executable = () -> restClient.getInstitutionByExternalId(externalId);
        //then
        assertThrows(ResourceNotFoundException.class, executable);
    }

    @Test
    void createInstitutionUsingExternalId() {
        //given
        String externalId = "externalId";
        //when
        Institution response = restClient.createInstitutionUsingExternalId(externalId);
        //then
        assertNotNull(response);
        checkNotNullFields(response);
    }

    @Test
    void createInstitutionRaw() {
        //given
        String externalId = "externalId";
        InstitutionSeed institutionSeed = mockInstance(new InstitutionSeed());
        institutionSeed.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        //when
        Institution response = restClient.createInstitutionRaw(externalId, institutionSeed);
        //then
        assertNotNull(response);
        checkNotNullFields(response);
    }


    @Test
    void onboardingImportOrganization_fullyValued() {
        // given
        OnboardingImportInstitutionRequest onboardingRequest = new OnboardingImportInstitutionRequest();
        onboardingRequest.setInstitutionExternalId(testCase2instIdMap.get(TestCase.FULLY_VALUED));
        onboardingRequest.setUsers(List.of(mockInstance(new User())));
        // when
        Executable executable = () -> restClient.onboardingImportOrganization(onboardingRequest);
        // then
        assertDoesNotThrow(executable);
    }


    @Test
    void onboardingImportOrganization_fullyNull() {
        // given
        OnboardingImportInstitutionRequest onboardingRequest = new OnboardingImportInstitutionRequest();
        onboardingRequest.setInstitutionExternalId(testCase2instIdMap.get(TestCase.FULLY_NULL));
        onboardingRequest.setUsers(List.of(mockInstance(new User())));
        // when
        Executable executable = () -> restClient.onboardingImportOrganization(onboardingRequest);
        // then
        assertDoesNotThrow(executable);
    }

    @Test
    void autoApprovalOnboardingOrganization_fullyValued() {
        // given
        OnboardingImportInstitutionRequest onboardingRequest = new OnboardingImportInstitutionRequest();
        onboardingRequest.setInstitutionExternalId(testCase2instIdMap.get(TestCase.FULLY_VALUED));
        onboardingRequest.setUsers(List.of(mockInstance(new User())));
        // when
        Executable executable = () -> restClient.autoApprovalOnboardingOrganization(onboardingRequest);
        // then
        assertDoesNotThrow(executable);
    }

    @Test
    void autoApprovalOnboardingImportOrganization_fullyNull() {
        // given
        OnboardingImportInstitutionRequest onboardingRequest = new OnboardingImportInstitutionRequest();
        onboardingRequest.setInstitutionExternalId(testCase2instIdMap.get(TestCase.FULLY_NULL));
        onboardingRequest.setUsers(List.of(mockInstance(new User())));
        // when
        Executable executable = () -> restClient.autoApprovalOnboardingOrganization(onboardingRequest);
        Executable executable = () -> restClient.onboardingImportOrganization(onboardingRequest);
        // then
        assertDoesNotThrow(executable);
    }

    @Test
    void getInstitution_fullyValued() {
        // given
        String id = testCase2instIdMap.get(TestCase.FULLY_VALUED);
        // when
        Institution response = restClient.getInstitution(id);
        assertNotNull(response);
        TestUtils.checkNotNullFields(response);
        response.getAttributes().forEach(TestUtils::checkNotNullFields);

    }


    @Test
    void getInstitution_fullyNull() {
        // given
        String id = testCase2instIdMap.get(TestCase.FULLY_NULL);
        // when
        Institution response = restClient.getInstitution(id);
        assertNotNull(response);
        assertNull(response.getAddress());
        assertNull(response.getDescription());
        assertNull(response.getDigitalAddress());
        assertNull(response.getId());
        assertNull(response.getExternalId());
        assertNull(response.getTaxCode());
        assertNull(response.getZipCode());
    }


}