package it.pagopa.selfcare.external_api.connector.rest.config;

import it.pagopa.selfcare.commons.connector.rest.config.RestClientBaseConfig;
import it.pagopa.selfcare.external_api.connector.rest.client.MsOnboardingControllerApi;
import it.pagopa.selfcare.external_api.connector.rest.client.MsOnboardingTokenControllerApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Import(RestClientBaseConfig.class)
@EnableFeignClients(clients = {MsOnboardingTokenControllerApi.class, MsOnboardingControllerApi.class})
@PropertySource("classpath:config/ms-onboarding-rest-client.properties")
public class MsOnboardingApiClientConfig {
}
