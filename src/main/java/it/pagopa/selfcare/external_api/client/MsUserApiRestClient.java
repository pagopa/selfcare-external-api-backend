package it.pagopa.selfcare.external_api.client;

import it.pagopa.selfcare.user.generated.openapi.v1.api.UserApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "${rest-client.ms-user-api.serviceCode}", url = "${rest-client.ms-user-api.base-url}")
public interface MsUserApiRestClient extends UserApi {
}
