package it.pagopa.selfcare.external_api.model.onboarding;

import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import it.pagopa.selfcare.external_api.model.institutions.RootParent;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Data
public class OnboardingData {

    private String institutionExternalId;
    private String taxCode;
    private String subunitCode;
    private String subunitType;
    private String productId;
    private String productName;
    private List<User> users;
    private String contractPath;
    private String contractVersion;
    private Billing billing;
    private InstitutionUpdate institutionUpdate;
    private InstitutionType institutionType;
    private String origin;
    private String ivassCode;
    private String pricingPlan;
    private RootParent rootParent;
    private Boolean sendCompleteOnboardingEmail;
    private InstitutionLocation location;
    private OnboardingImportContract contractImported;

    public List<User> getUsers() {
        return Optional.ofNullable(users).orElse(Collections.emptyList());
    }

}
