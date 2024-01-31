package it.pagopa.selfcare.external_api.connector.rest.client;

import it.pagopa.selfcare.onboarding.generated.openapi.v1.api.OnboardingControllerApi;
import it.pagopa.selfcare.onboarding.generated.openapi.v1.api.TokenControllerApi;
import org.springframework.cloud.openfeign.FeignClient;


@FeignClient(name = "${rest-client.ms-onboarding-controller-api.serviceCode}", url = "${rest-client.ms-onboarding.base-url}")
public interface MsOnboardingControllerApi extends OnboardingControllerApi {
}
