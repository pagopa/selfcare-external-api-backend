package it.pagopa.selfcare.external_api.connector.rest.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import it.pagopa.selfcare.commons.connector.rest.BaseFeignRestClientTest;
import it.pagopa.selfcare.commons.connector.rest.RestTestUtils;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.external_api.connector.rest.config.PartyManagementRestClientTestConfig;
import it.pagopa.selfcare.external_api.connector.rest.model.PartyRole;
import it.pagopa.selfcare.external_api.connector.rest.model.institution.RelationshipsResponse;
import it.pagopa.selfcare.external_api.model.user.RelationshipState;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.commons.httpclient.HttpClientConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.TestPropertySourceUtils;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static it.pagopa.selfcare.external_api.connector.rest.model.PartyRole.MANAGER;
import static it.pagopa.selfcare.external_api.connector.rest.model.PartyRole.OPERATOR;
import static it.pagopa.selfcare.external_api.model.user.RelationshipState.ACTIVE;
import static it.pagopa.selfcare.external_api.model.user.RelationshipState.PENDING;
import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource(
        locations = "classpath:config/party-management-rest-client.properties",
        properties = {
                "logging.level.it.pagopa.selfcare.external-api.connector.rest=DEBUG",
                "spring.application.name=selc-external-api-connector-rest",
                "feign.okhttp.enabled=true"
        }
)
@ContextConfiguration(
        initializers = PartyManagementRestClientTest.RandomPortInitializer.class,
        classes = {PartyManagementRestClientTestConfig.class, HttpClientConfiguration.class}
)
class PartyManagementRestClientTest extends BaseFeignRestClientTest {

    @Order(1)
    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(RestTestUtils.getWireMockConfiguration("stubs/party-management")
//                    .notifier(new ConsoleNotifier(false))
//                    .gzipDisabled(true)
//                    .disableRequestJournal()
//                    .useChunkedTransferEncoding(Options.ChunkedEncodingPolicy.BODY_FILE)
            )
//            .configureStaticDsl(true)
            .build();

    public static class RandomPortInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @SneakyThrows
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                    String.format("USERVICE_PARTY_MANAGEMENT_URL=%s/pdnd-interop-uservice-party-management/0.0.1",
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
    private PartyManagementRestClient restClient;

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
        RelationshipsResponse response = restClient.getInstitutionRelationships(userId, externalId, roles, states, products, productRole);
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
    void getInstitutionRelationships_fullyEmpty() {
        // given
        String externalId = testCase2instIdMap.get(TestCase.EMPTY_RESULT);
        EnumSet<PartyRole> roles = EnumSet.of(MANAGER, OPERATOR);
        EnumSet<RelationshipState> states = EnumSet.of(ACTIVE, PENDING);
        Set<String> products = Set.of("prod1", "prod2");
        Set<String> productRole = Set.of("api", "security");
        String userId = "userId";
        // when
        RelationshipsResponse response = restClient.getInstitutionRelationships(userId, externalId, roles, states, products, productRole);
        // then
        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    void getInstitutionRelationships_fullyValued() {
        // given
        String externalId = testCase2instIdMap.get(TestCase.FULLY_VALUED);
        EnumSet<PartyRole> roles = EnumSet.of(MANAGER, OPERATOR);
        EnumSet<RelationshipState> states = EnumSet.of(ACTIVE, PENDING);
        Set<String> products = Set.of("prod1", "prod2");
        Set<String> productRole = Set.of("api", "security");
        String userId = "userId";
        // when
        RelationshipsResponse response = restClient.getInstitutionRelationships(userId, externalId, roles, states, products, productRole);
        // then
        assertNotNull(response);
        assertFalse(response.isEmpty());
        response.forEach(relationshipInfo -> {
            TestUtils.checkNotNullFields(relationshipInfo);
            TestUtils.checkNotNullFields(relationshipInfo.getInstitutionUpdate());
            TestUtils.checkNotNullFields(relationshipInfo.getBilling());
        });
    }
}