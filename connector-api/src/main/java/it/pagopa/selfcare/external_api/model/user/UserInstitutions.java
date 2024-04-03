package it.pagopa.selfcare.external_api.model.user;

import lombok.Data;

import javax.validation.Valid;
import java.util.List;

@Data
public class UserInstitutions {
    private String institutionId;

    private String userMailUuid;

    @Valid
    private List<OnboardedProductResponse> products = null;
}
