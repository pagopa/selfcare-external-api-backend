package it.pagopa.selfcare.external_api.web.model.onboarding;

import io.swagger.annotations.ApiModelProperty;

public class CompanyInformationsDto {

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.companyInformations.rea}")
    private String rea;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.companyInformations.shareCapital}")
    private String shareCapital;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.companyInformations.businessRegisterPlace}")
    private String businessRegisterPlace;

}
