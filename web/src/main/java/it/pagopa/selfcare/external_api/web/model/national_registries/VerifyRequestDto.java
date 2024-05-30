package it.pagopa.selfcare.external_api.web.model.national_registries;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class VerifyRequestDto {
    @JsonProperty(required = true)
    @NotBlank
    private String taxId;
    @JsonProperty(required = true)
    @NotBlank
    private String vatNumber;
}
