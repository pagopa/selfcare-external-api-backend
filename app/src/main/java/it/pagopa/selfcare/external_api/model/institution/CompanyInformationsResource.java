package it.pagopa.selfcare.external_api.model.institution;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CompanyInformationsResource {

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.companyInformations.rea}")
    private String rea;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.companyInformations.shareCapital}")
    private String shareCapital;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.companyInformations.businessRegisterPlace}")
    private String businessRegisterPlace;

}
