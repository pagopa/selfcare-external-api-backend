package it.pagopa.selfcare.external_api.connector.rest.model.onboarding;

import lombok.Data;

@Data
public class OnboardingContractImport {

    private String fileName;
    private String filePath;
    private String contractType;

}
