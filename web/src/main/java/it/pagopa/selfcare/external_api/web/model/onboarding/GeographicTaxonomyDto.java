package it.pagopa.selfcare.external_api.web.model.onboarding;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class GeographicTaxonomyDto {

    @ApiModelProperty(value = "${swagger.external_api.geographicTaxonomy.model.code}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String code;

    @ApiModelProperty(value = "${swagger.external_api.geographicTaxonomy.model.desc}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String desc;

}
