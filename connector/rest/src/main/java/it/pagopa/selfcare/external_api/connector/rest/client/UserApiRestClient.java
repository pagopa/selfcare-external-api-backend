package it.pagopa.selfcare.external_api.connector.rest.client;

import it.pagopa.selfcare.user.generated.openapi.v1.api.UserControllerApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "${rest-client.user-ms.serviceCode}", url = "${rest-client.user-ms.base-url}")
public interface UserApiRestClient extends UserControllerApi {
}
