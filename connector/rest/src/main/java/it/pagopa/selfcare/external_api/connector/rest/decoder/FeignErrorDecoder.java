package it.pagopa.selfcare.external_api.connector.rest.decoder;

import feign.Response;
import feign.codec.ErrorDecoder;
import it.pagopa.selfcare.external_api.exception.ResourceNotFoundException;

public class FeignErrorDecoder extends ErrorDecoder.Default {

    @Override
    public Exception decode(String methodKey, Response response) {
        System.out.println(response.status());
        if (response.status() == 404) {
            throw new ResourceNotFoundException();
        } else {
            return super.decode(methodKey, response);
        }
    }
}
