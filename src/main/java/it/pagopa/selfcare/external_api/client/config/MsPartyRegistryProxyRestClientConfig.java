package it.pagopa.selfcare.external_api.client.config;

import it.pagopa.selfcare.commons.connector.rest.config.RestClientBaseConfig;
import it.pagopa.selfcare.external_api.client.MsPartyRegistryProxyRestClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Import(RestClientBaseConfig.class)
@EnableFeignClients(clients = MsPartyRegistryProxyRestClient.class)
@PropertySource("classpath:config/ms-party-registry-proxy-rest-client.properties")
public class MsPartyRegistryProxyRestClientConfig {
}
