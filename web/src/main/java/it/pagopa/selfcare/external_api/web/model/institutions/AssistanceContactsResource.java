package it.pagopa.selfcare.external_api.web.model.institutions;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AssistanceContactsResource {

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.assistance.supportEmail}")
    private String supportEmail;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.assistance.supportPhone}")
    private String supportPhone;

}
