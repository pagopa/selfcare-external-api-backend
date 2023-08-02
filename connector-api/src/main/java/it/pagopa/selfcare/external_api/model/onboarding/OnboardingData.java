package it.pagopa.selfcare.external_api.model.onboarding;

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
    private String pricingPlan;

    public List<User> getUsers() {
        return Optional.ofNullable(users).orElse(Collections.emptyList());
    }

}
