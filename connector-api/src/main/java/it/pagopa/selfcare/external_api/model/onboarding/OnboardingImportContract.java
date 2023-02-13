package it.pagopa.selfcare.external_api.model.onboarding;

import lombok.Data;

@Data
public class OnboardingImportContract {

    private String fileName;
    private String filePath;
    private String contractType;

}
