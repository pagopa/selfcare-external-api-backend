package it.pagopa.selfcare.external_api.core;

public interface OnboardingValidationStrategy {

    boolean validate(String productId, String institutionExternalId);

}
