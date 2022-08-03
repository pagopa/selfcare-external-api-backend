package it.pagopa.selfcare.external_api.web.model.products;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ProductResource {
    @ApiModelProperty(value = "${swagger.external_api.products.model.id}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String id;

    @ApiModelProperty(value = "${swagger.external_api.products.model.title}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String title;

    @ApiModelProperty(value = "${swagger.external_api.products.model.description}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String description;

    @ApiModelProperty("${swagger.external_api.products.model.urlPublic}")
    private String urlPublic;

    @ApiModelProperty(value = "${swagger.external_api.products.model.urlBO}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String urlBO;

}
