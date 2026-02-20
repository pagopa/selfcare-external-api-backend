package it.pagopa.selfcare.external_api.model.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import it.pagopa.selfcare.commons.base.security.PartyRole;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class UserDto {

    @Schema(description = "${swagger.external_api.user.model.name}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String name;

    @Schema(description = "${swagger.external_api.user.model.surname}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String surname;

    @Schema(description = "${swagger.external_api.user.model.fiscalCode}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String taxCode;

    @Schema(description = "${swagger.external_api.user.model.role}", required = true)
    @JsonProperty(required = true)
    @NotNull
    private PartyRole role;

    @Schema(description = "${swagger.external_api.user.model.email}", required = true)
    @JsonProperty(required = true)
    @NotNull
    @Email
    private String email;

    @Schema(hidden = true)
    private String productRole;

}
