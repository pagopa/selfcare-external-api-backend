package it.pagopa.selfcare.external_api.connector.rest.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import it.pagopa.selfcare.commons.connector.rest.BaseFeignRestClientTest;
import it.pagopa.selfcare.commons.connector.rest.RestTestUtils;
import it.pagopa.selfcare.external_api.connector.rest.config.ProductsRestClientTestConfig;
import it.pagopa.selfcare.external_api.model.onboarding.InstitutionType;
import it.pagopa.selfcare.external_api.model.product.Product;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

@TestPropertySource(
        locations = "classpath:config/products-rest-client.properties",
        properties = {
                "logging.level.it.pagopa.selfcare.dashboard.connector.rest=DEBUG",
                "spring.application.name=selc-dashboard-connector-rest",
                "feign.okhttp.enabled=true"
        })
@ContextConfiguration(
        initializers = ProductsRestClientTest.RandomPortInitializer.class,
        classes = {ProductsRestClientTestConfig.class, HttpClientConfiguration.class})
class ProductsRestClientTest extends BaseFeignRestClientTest {
    @Order(1)
    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(RestTestUtils.getWireMockConfiguration("stubs/products"))
            .build();

    @Autowired
    private ProductsRestClient restClient;


    public static class RandomPortInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @SneakyThrows
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                    String.format("MS_PRODUCT_URL=%s",
                            wm.getRuntimeInfo().getHttpBaseUrl())
            );
        }
    }


    @Test
    void getProducts() {
        // given and when
        List<Product> response = restClient.getProducts();
        // then
        assertFalse(response.isEmpty());
    }

    @Test
    void getProduct() {
        // given and when
        Product response = restClient.getProduct("testProductId");
        // then
        assertFalse(response.getTitle().isEmpty());
    }

    @Test
    void getProductNoInstitutionType_fullyValued() {
        // given
        String id = "testProductId3";
        // when
        Product product = restClient.getProduct(id, null);
        // then
        Assertions.assertNotNull(product);
        Assertions.assertNotNull(product.getId());
        Assertions.assertNotNull(product.getRoleMappings());
        Assertions.assertNotNull(product.getTitle());
        Assertions.assertFalse(product.getRoleMappings().isEmpty());
    }

    @Test
    void getProductWithInstitutionType_fullyValued() {
        // given
        String id = "testProductId2";
        InstitutionType institutionType = InstitutionType.PA;
        // when
        Product product = restClient.getProduct(id, institutionType);
        // then
        Assertions.assertNotNull(product);
        Assertions.assertNotNull(product.getId());
        Assertions.assertNotNull(product.getRoleMappings());
        Assertions.assertNotNull(product.getTitle());
        Assertions.assertFalse(product.getRoleMappings().isEmpty());
    }
}