package it.pagopa.selfcare.external_api.connector.rest.decoder;

import feign.Request;
import feign.Response;
import feign.codec.ErrorDecoder;
import it.pagopa.selfcare.external_api.exceptions.InstitutionDoesNotExistException;
import it.pagopa.selfcare.external_api.exceptions.InvalidRequestException;
import it.pagopa.selfcare.external_api.exceptions.ResourceNotFoundException;

import java.util.Optional;

public class FeignErrorDecoder extends ErrorDecoder.Default {

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 404) {
            throw new ResourceNotFoundException(response.reason());
        } else if (response.status() == 400 && response.request().httpMethod().equals(Request.HttpMethod.HEAD)) {
            throw new InstitutionDoesNotExistException();
        } else if(response.status() == 400) {
            String errorMessage = Optional.ofNullable(response.body()).map(Object::toString).orElse(null);
            throw new InvalidRequestException(errorMessage);
        } else {
            return super.decode(methodKey, response);
        }
    }
}
