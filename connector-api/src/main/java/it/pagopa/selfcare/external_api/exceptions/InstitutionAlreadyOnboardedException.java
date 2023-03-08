package it.pagopa.selfcare.external_api.exceptions;

public class InstitutionAlreadyOnboardedException extends RuntimeException {

    public InstitutionAlreadyOnboardedException(String message) {
        super(message);
    }

}
