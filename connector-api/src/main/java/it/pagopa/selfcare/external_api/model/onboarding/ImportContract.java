package it.pagopa.selfcare.external_api.model.onboarding;

import lombok.Data;

@Data
public class ImportContract {

    private String fileName;
    private String filePath;
    private String contractType;

}
