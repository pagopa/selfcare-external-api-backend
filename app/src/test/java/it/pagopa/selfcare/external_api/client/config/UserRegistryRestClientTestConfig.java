package it.pagopa.selfcare.external_api.client.config;

import it.pagopa.selfcare.external_api.client.config.UserRegistryRestClientConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import(UserRegistryRestClientConfig.class)
public class UserRegistryRestClientTestConfig {
}
