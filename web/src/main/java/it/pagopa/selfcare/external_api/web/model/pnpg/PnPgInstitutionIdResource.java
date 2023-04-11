package it.pagopa.selfcare.external_api.web.model.pnpg;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
public class PnPgInstitutionIdResource {
    @ApiModelProperty(value = "${swagger.external_api.institutions.model.id}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String id;
}
