package it.pagopa.selfcare.external_api.model.onboarding;

import it.pagopa.selfcare.external_api.model.user.Person;
import lombok.Data;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Optional;

@Data
public class OnboardingInstitutionUsersRequest {

    @NotEmpty(message = "productId is required")
    private String productId;

    @NotEmpty(message = "at least one user is required")
    private List<Person> users;

    private String institutionId;

    private String institutionTaxCode;

    private String institutionSubunitCode;

    private Boolean sendCreateUserNotificationEmail = Boolean.TRUE;

    @AssertTrue(message = "at least one of institutionId or institutionTaxCode must be present")
    public boolean isIdOrTaxcodeNotEmpty() {
        final boolean isIdNotEmpty = Optional.ofNullable(institutionId).map(id -> !id.isEmpty()).orElse(false);
        final boolean isTaxcodeNotEmpty = Optional.ofNullable(institutionTaxCode).map(taxcode -> !taxcode.isEmpty()).orElse(false);
        return isIdNotEmpty || isTaxcodeNotEmpty;
    }

}
