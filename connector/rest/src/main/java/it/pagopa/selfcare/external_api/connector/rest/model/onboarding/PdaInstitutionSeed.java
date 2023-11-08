package it.pagopa.selfcare.external_api.connector.rest.model.onboarding;

import it.pagopa.selfcare.external_api.model.onboarding.PdaOnboardingData;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class PdaInstitutionSeed {

    public PdaInstitutionSeed(PdaOnboardingData onboardingData) {
        description = onboardingData.getDescription();
        taxCode = onboardingData.getTaxCode();
        injectionInstitutionType = onboardingData.getInjectionInstitutionType();
    }

    private String description;
    private String taxCode;
    private String injectionInstitutionType;
}
