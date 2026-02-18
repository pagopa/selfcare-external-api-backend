package it.pagopa.selfcare.external_api.model.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@EqualsAndHashCode(of = "code")
@NoArgsConstructor
public class ProductRole {

    @ApiModelProperty(value = "${swagger.external_api.product-role.model.code}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String code;

    @ApiModelProperty(value = "${swagger.external_api.product-role.model.label}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String label;

    @ApiModelProperty(value = "${swagger.external_api.product-role.model.description}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String description;


    public ProductRole(ProductRoleInfo.ProductRole productRole) {
        code = productRole.getCode();
        label = productRole.getLabel();
        description = productRole.getDescription();
    }

}