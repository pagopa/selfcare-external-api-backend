package it.pagopa.selfcare.external_api.model.onboarding;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Email;

@Data
public class AssistanceContactsDto {

    @Schema(description = "${swagger.external_api.institutions.model.assistance.supportEmail}")
    @Email
    private String supportEmail;

    @Schema(description = "${swagger.external_api.institutions.model.assistance.supportPhone}")
    private String supportPhone;

}
