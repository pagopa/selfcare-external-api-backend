package it.pagopa.selfcare.external_api.web.model.products;

import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.commons.base.security.PartyRole;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.time.Instant;
import java.util.EnumMap;

@Data
public class ProductResource {
    @ApiModelProperty(value = "${swagger.external_api.products.model.id}", required = true)
    @NotBlank
    private String id;

    @ApiModelProperty(value = "${swagger.external_api.products.model.title}", required = true)
    @NotBlank
    private String title;

    @ApiModelProperty(value = "${swagger.external_api.products.model.contractTemplatePath}", required = true)
    @NotBlank
    private String contractTemplatePath;

    @ApiModelProperty(value = "${swagger.external_api.products.model.contractTemplateUpdatedAt}")
    private Instant contractTemplateUpdatedAt;

    @ApiModelProperty(value = "${swagger.external_api.products.model.contractTemplateVersion}", required = true)
    @NotBlank
    private String contractTemplateVersion;

    @ApiModelProperty(value = "${swagger.external_api.products.model.createdAt}")
    private Instant createdAt;

    @ApiModelProperty(value = "${swagger.external_api.products.model.description}", required = true)
    @NotBlank
    private String description;

    @ApiModelProperty("${swagger.external_api.products.model.urlPublic}")
    private String urlPublic;

    @ApiModelProperty(value = "${swagger.external_api.products.model.urlBO}", required = true)
    @NotBlank
    private String urlBO;

    @ApiModelProperty(value = "${swagger.external_api.products.model.depictImageUrl}")
    private String depictImageUrl;

    @ApiModelProperty(value = "${swagger.external_api.products.model.identityTokenAudience}")
    private String identityTokenAudience;

    @ApiModelProperty(value = "${swagger.external_api.products.model.logo}")
    private String logo;

    @ApiModelProperty(value = "${swagger.external_api.products.model.logoBgColor}")
    private String logoBgColor;

    @ApiModelProperty(value = "${swagger.external_api.products.model.parentId}")
    private String parentId;

    @ApiModelProperty(value = "${swagger.external_api.products.model.roleMappings}", required = true)
    @NotEmpty
    private EnumMap<PartyRole, ProductRoleInfo> roleMappings;

    @ApiModelProperty(value = "${swagger.external_api.products.model.roleManagementURL}")
    private String roleManagementURL;

}
