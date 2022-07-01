package it.pagopa.selfcare.external_api.connector.rest.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import it.pagopa.selfcare.commons.connector.rest.BaseFeignRestClientTest;
import it.pagopa.selfcare.commons.connector.rest.RestTestUtils;
import it.pagopa.selfcare.external_api.connector.rest.config.UserGroupRestClientTestConfig;
import it.pagopa.selfcare.external_api.connector.rest.model.UserGroupResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.commons.httpclient.HttpClientConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.TestPropertySourceUtils;

import java.util.List;
import java.util.UUID;

@TestPropertySource(
        locations = "classpath:config/user-group-rest-client.properties",
        properties = {
                "logging.level.it.pagopa.selfcare.external_api.connector.rest=DEBUG",
                "spring.application.name=selc-external-api-connector-rest",
                "feign.okhttp.enabled=true"
        }
)
@ContextConfiguration(
        initializers = UserGroupRestClientTest.RandomPortInitializer.class,
        classes = {UserGroupRestClientTestConfig.class, HttpClientConfiguration.class})
class UserGroupRestClientTest extends BaseFeignRestClientTest {

    @Order(1)
    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(RestTestUtils.getWireMockConfiguration("stubs/user-group"))
            .build();


    public static class RandomPortInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @SneakyThrows
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                    String.format("MS_USER_GROUP_URL=%s",
                            wm.getRuntimeInfo().getHttpBaseUrl())
            );
        }
    }

    @Autowired
    private UserGroupRestClient restClient;

    @Test
    void getUserGroups_fullyValued() {
        //given
        String institutionId = null;
        String productId = null;
        UUID userId = null;
        Pageable pageable = Pageable.unpaged();

        List<UserGroupResponse> response = restClient.getUserGroups(institutionId, productId, userId, pageable);
        //then
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.get(0).getCreatedAt());
        Assertions.assertNotNull(response.get(0).getCreatedBy());
        Assertions.assertNotNull(response.get(0).getDescription());
        Assertions.assertNotNull(response.get(0).getId());
        Assertions.assertNotNull(response.get(0).getMembers());
        Assertions.assertNotNull(response.get(0).getName());
        Assertions.assertNotNull(response.get(0).getInstitutionId());
        Assertions.assertNotNull(response.get(0).getModifiedAt());
        Assertions.assertNotNull(response.get(0).getModifiedBy());
        Assertions.assertNotNull(response.get(0).getStatus());
    }

    @Test
    void getUserGroups_fullyValuedPageable() {
        //given
        String institutionId = null;
        String productId = null;
        UUID userId = null;
        Pageable pageable = PageRequest.of(0, 1, Sort.by("name"));

        List<UserGroupResponse> response = restClient.getUserGroups(institutionId, productId, userId, pageable);
        //then
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.get(0).getCreatedAt());
        Assertions.assertNotNull(response.get(0).getCreatedBy());
        Assertions.assertNotNull(response.get(0).getDescription());
        Assertions.assertNotNull(response.get(0).getId());
        Assertions.assertNotNull(response.get(0).getMembers());
        Assertions.assertNotNull(response.get(0).getName());
        Assertions.assertNotNull(response.get(0).getInstitutionId());
        Assertions.assertNotNull(response.get(0).getModifiedAt());
        Assertions.assertNotNull(response.get(0).getModifiedBy());
        Assertions.assertNotNull(response.get(0).getStatus());
    }

}