package it.pagopa.selfcare.external_api.client.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import(MsPartyRegistryProxyRestClientConfig.class)
public class MsPartyRegistryProxyRestClientTestConfig {
}
