package it.pagopa.selfcare.external_api.web.model.onboarding;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Email;

@Data
public class AssistanceContactsDto {

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.assistance.supportEmail}")
    @Email
    private String supportEmail;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.assistance.supportPhone}")
    private String supportPhone;

}
