package it.pagopa.selfcare.external_api.model.pnpg;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PnPgInstitutionIdResource {
    @ApiModelProperty(value = "${swagger.external_api.institutions.model.id}")
    private String id;
}
