package it.pagopa.selfcare.external_api.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:config/core-config.properties")
public class CoreConfig {
}
