package it.pagopa.selfcare.external_api.web.model.products;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;


@Data
@NoArgsConstructor
public class ProductRoleInfo {

    @ApiModelProperty(value = "${swagger.external_api.product-role-info.model.multiroleAllowed}", required = true)
    @JsonProperty(required = true)
    @NotNull
    private Boolean multiroleAllowed;

    @ApiModelProperty(value = "${swagger.external_api.product-role-info.model.roles}", required = true)
    @JsonProperty(required = true)
    @NotEmpty
    @Valid
    private List<ProductRole> roles;

    public ProductRoleInfo(it.pagopa.selfcare.external_api.model.product.ProductRoleInfo productRoleInfo) {
        multiroleAllowed = productRoleInfo.isMultiroleAllowed();
        setRoles(productRoleInfo.getRoles());
    }

    public void setRoles(List<it.pagopa.selfcare.external_api.model.product.ProductRoleInfo.ProductRole> roles) {
        if (roles != null) {
            this.roles = roles.stream()
                    .map(ProductRole::new)
                    .collect(Collectors.toList());
        }
    }
}
