package it.pagopa.selfcare.external_api.core.strategy;

public interface OnboardingValidationStrategy {

    boolean validate(String productId, String institutionExternalId);

}
