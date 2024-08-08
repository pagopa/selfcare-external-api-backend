package it.pagopa.selfcare.external_api.client.config;

import it.pagopa.selfcare.external_api.client.config.MsCoreRestClientConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import(MsCoreRestClientConfig.class)
public class MsCoreRestClientTestConfig {
}
