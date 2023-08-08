package it.pagopa.selfcare.external_api.web.model.institutions;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RootParentResource {
    @ApiModelProperty(value = "${swagger.external_api.institutions.model.id}")
    private String id;
    @ApiModelProperty("swagger.external_api.institutions.model.parentDescription")
    private String description;
}
