package it.pagopa.selfcare.external_api.client;

import it.pagopa.selfcare.core.generated.openapi.v1.api.PersonsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * Ms Core Rest Client
 */
@FeignClient(name = "${rest-client.ms-core-user-api.serviceCode}", url = "${rest-client.ms-core.base-url}")
public interface MsCoreUserApiClient extends PersonsApi {
}
