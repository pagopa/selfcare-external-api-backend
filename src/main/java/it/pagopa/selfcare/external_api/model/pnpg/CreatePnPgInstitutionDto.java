package it.pagopa.selfcare.external_api.model.pnpg;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class CreatePnPgInstitutionDto {
    @ApiModelProperty(value = "${swagger.external_api.institutions.model.externalId}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String externalId;
    @ApiModelProperty(value = "${swagger.external_api.institutions.model.name}")
    private String description;
}
