package it.pagopa.selfcare.external_api.model.institution;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class InstitutionLocationDataDto {
    @ApiModelProperty(value = "${swagger.external_api.institutions.model.city}")
    private String city;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.county}")
    private String county;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.country}")
    private String country;

}

