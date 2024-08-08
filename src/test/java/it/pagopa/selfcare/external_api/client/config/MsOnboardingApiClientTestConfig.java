package it.pagopa.selfcare.external_api.client.config;

import it.pagopa.selfcare.external_api.client.config.MsOnboardingApiClientConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import(MsOnboardingApiClientConfig.class)
public class MsOnboardingApiClientTestConfig {
}
