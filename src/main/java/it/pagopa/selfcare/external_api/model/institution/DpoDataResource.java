package it.pagopa.selfcare.external_api.model.institution;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DpoDataResource {

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.pspData.dpoData.address}")
    private String address;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.pspData.dpoData.pec}")
    private String pec;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.pspData.dpoData.email}")
    private String email;

}
