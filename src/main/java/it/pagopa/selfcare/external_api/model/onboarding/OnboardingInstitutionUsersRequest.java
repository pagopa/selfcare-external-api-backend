package it.pagopa.selfcare.external_api.model.onboarding;

import it.pagopa.selfcare.external_api.model.user.Person;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class OnboardingInstitutionUsersRequest {

    @NotEmpty(message = "productId is required")
    private String productId;

    @NotEmpty(message = "at least one user is required")
    private List<Person> users;

    @NotEmpty(message = "InstitutionId is required")
    private String institutionTaxCode;

    private String institutionSubunitCode;
    private Boolean sendCreateUserNotificationEmail = Boolean.TRUE;

}
