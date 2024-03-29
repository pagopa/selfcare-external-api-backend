package it.pagopa.selfcare.external_api.connector.rest.config;

import it.pagopa.selfcare.commons.connector.rest.config.RestClientBaseConfig;
import it.pagopa.selfcare.external_api.connector.rest.client.MsCoreInstitutionApiClient;
import it.pagopa.selfcare.external_api.connector.rest.client.MsCoreRestClient;
import it.pagopa.selfcare.external_api.connector.rest.client.MsCoreUserApiClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Import(RestClientBaseConfig.class)
@EnableFeignClients(clients = {MsCoreRestClient.class, MsCoreInstitutionApiClient.class, MsCoreUserApiClient.class})
@PropertySource("classpath:config/ms-core-rest-client.properties")
public class MsCoreRestClientConfig {
}
