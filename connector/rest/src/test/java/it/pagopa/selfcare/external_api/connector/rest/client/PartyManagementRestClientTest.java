package it.pagopa.selfcare.external_api.connector.rest.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import it.pagopa.selfcare.commons.connector.rest.RestTestUtils;
import it.pagopa.selfcare.external_api.connector.rest.config.PartyManagementRestClientTestConfig;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.cloud.commons.httpclient.HttpClientConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.TestPropertySourceUtils;

@TestPropertySource(
        locations = "classpath:config/party-management.rest-client.properties",
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
class PartyManagementRestClientTest {

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
}