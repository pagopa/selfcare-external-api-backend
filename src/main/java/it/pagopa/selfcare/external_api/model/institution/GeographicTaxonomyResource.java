package it.pagopa.selfcare.external_api.model.institution;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class GeographicTaxonomyResource {
    @ApiModelProperty(value = "${swagger.external_api.geographicTaxonomy.model.code}")
    private String code;

    @ApiModelProperty(value = "${swagger.external_api.geographicTaxonomy.model.desc}")
    private String desc;
}
