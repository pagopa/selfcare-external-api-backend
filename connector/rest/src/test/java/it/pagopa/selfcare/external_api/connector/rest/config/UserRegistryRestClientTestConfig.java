package it.pagopa.selfcare.external_api.connector.rest.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import(UserRegistryRestClientConfig.class)
public class UserRegistryRestClientTestConfig {
}
