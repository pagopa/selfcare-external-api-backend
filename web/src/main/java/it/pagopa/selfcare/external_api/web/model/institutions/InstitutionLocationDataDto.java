package it.pagopa.selfcare.external_api.web.model.institutions;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class InstitutionLocationDataDto {
    @ApiModelProperty(value = "${swagger.external_api.institutions.model.city}", required = true)
    private String city;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.county}", required = true)
    private String county;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.country}", required = true)
    private String country;

}

