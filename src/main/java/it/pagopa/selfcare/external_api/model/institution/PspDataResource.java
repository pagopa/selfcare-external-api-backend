package it.pagopa.selfcare.external_api.model.institution;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class PspDataResource {

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.pspData.businessRegisterNumber}")
    private String businessRegisterNumber;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.pspData.legalRegisterName}")
    private String legalRegisterName;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.pspData.legalRegisterNumber}")
    private String legalRegisterNumber;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.pspData.abiCode}")
    private String abiCode;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.pspData.vatNumberGroup}")
    private Boolean vatNumberGroup;

}
