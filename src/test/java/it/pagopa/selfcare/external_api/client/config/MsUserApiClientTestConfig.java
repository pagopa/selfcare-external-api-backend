package it.pagopa.selfcare.external_api.client.config;

import it.pagopa.selfcare.external_api.client.config.MsUserApiClientConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import(MsUserApiClientConfig.class)
public class MsUserApiClientTestConfig {
}
