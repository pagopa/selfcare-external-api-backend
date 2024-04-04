package it.pagopa.selfcare.external_api.connector.rest.config;

import it.pagopa.selfcare.commons.connector.rest.config.RestClientBaseConfig;
import it.pagopa.selfcare.external_api.connector.rest.client.UserApiRestClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Import(RestClientBaseConfig.class)
@EnableFeignClients(clients = UserApiRestClient.class)
@PropertySource("classpath:config/user-rest-client.properties")
public class UserApiRestClientConfig {
}
