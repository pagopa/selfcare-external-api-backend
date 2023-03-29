package it.pagopa.selfcare.external_api.connector.rest.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.commons.connector.rest.BaseFeignRestClientTest;
import it.pagopa.selfcare.commons.connector.rest.RestTestUtils;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.external_api.connector.rest.config.PartyManagementRestClientTestConfig;
import it.pagopa.selfcare.external_api.connector.rest.model.institution.Institutions;
import it.pagopa.selfcare.external_api.model.institutions.SearchMode;
import it.pagopa.selfcare.external_api.model.relationship.Relationships;
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

import java.util.EnumSet;
import java.util.Set;

import static it.pagopa.selfcare.commons.base.security.PartyRole.MANAGER;
import static it.pagopa.selfcare.commons.base.security.PartyRole.OPERATOR;
import static it.pagopa.selfcare.commons.utils.TestUtils.checkNotNullFields;
import static it.pagopa.selfcare.external_api.model.user.RelationshipState.ACTIVE;
import static it.pagopa.selfcare.external_api.model.user.RelationshipState.PENDING;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestPropertySource(
        locations = "classpath:config/party-management-rest-client.properties",
        properties = {
                "logging.level.it.pagopa.selfcare.external_api.connector.rest=DEBUG",
                "spring.application.name=selc-external-api-connector-rest",
                "feign.okhttp.enabled=true"
        })
@ContextConfiguration(
        initializers = PartyManagementRestClientTest.RandomPortInitializer.class,
        classes = {PartyManagementRestClientTestConfig.class, HttpClientConfiguration.class})
class PartyManagementRestClientTest extends BaseFeignRestClientTest {

    @Order(1)
    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(RestTestUtils.getWireMockConfiguration("stubs/party-management"))
            .build();


    static class RandomPortInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @SneakyThrows
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                    String.format("USERVICE_PARTY_MANAGEMENT_URL=%s/pdnd-interop-uservice-party-management/0.0.1",
                            wm.getRuntimeInfo().getHttpBaseUrl())
            );
        }
    }


    @Autowired
    private PartyManagementRestClient restClient;


    @Test
    void getRelationships_fullyValued() {
        // given
        String from = "from";
        String to = "to";
        EnumSet<PartyRole> roles = EnumSet.of(MANAGER, OPERATOR);
        EnumSet<RelationshipState> states = EnumSet.of(ACTIVE, PENDING);
        Set<String> products = Set.of("prod1", "prod2");
        Set<String> productRole = Set.of("api", "security");
        // when
        final Relationships response = restClient.getRelationships(from, to, roles, states, products, productRole);
        // then
        assertNotNull(response);
        assertNotNull(response.getItems());
        assertFalse(response.getItems().isEmpty());
        response.getItems().forEach(relationship -> {
            checkNotNullFields(relationship);
            checkNotNullFields(relationship.getInstitutionUpdate());
            checkNotNullFields(relationship.getBilling());
            checkNotNullFields(relationship.getProduct());
        });
    }

    @Test
    void getInstitutionsByGeoTaxonomies_fullyValued() {
        //given
        final String geoTaxonomies = "geoTax";
        final SearchMode searchMode = SearchMode.any;
        //when
        final Institutions response = restClient.getInstitutionsByGeoTaxonomies(geoTaxonomies, searchMode);
        //then
        assertNotNull(response);
        assertNotNull(response.getItems());
        assertFalse(response.getItems().isEmpty());
        response.getItems().forEach(institution -> {
            institution.getGeographicTaxonomies().forEach(TestUtils::checkNotNullFields);
            checkNotNullFields(institution);
        });
    }

}