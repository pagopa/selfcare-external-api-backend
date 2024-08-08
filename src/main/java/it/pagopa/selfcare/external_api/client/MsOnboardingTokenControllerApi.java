package it.pagopa.selfcare.external_api.client;

import it.pagopa.selfcare.onboarding.generated.openapi.v1.api.TokenControllerApi;
import org.springframework.cloud.openfeign.FeignClient;


@FeignClient(name = "${rest-client.ms-onboarding-token-api.serviceCode}", url = "${rest-client.ms-onboarding.base-url}")
public interface MsOnboardingTokenControllerApi extends TokenControllerApi {
}
