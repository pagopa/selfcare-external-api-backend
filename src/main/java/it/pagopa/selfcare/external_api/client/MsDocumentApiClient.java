package it.pagopa.selfcare.external_api.client;

import it.pagopa.selfcare.document.generated.openapi.v1.api.DocumentControllerApi;
import org.springframework.cloud.openfeign.FeignClient;

public interface MsDocumentApiClient extends DocumentControllerApi {
}
