package it.pagopa.selfcare.external_api.web.model.products;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.commons.base.security.PartyRole;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.EnumMap;

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

    @ApiModelProperty(value = "${swagger.external_api.products.model.contractTemplatePath}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String contractTemplatePath;

    @ApiModelProperty(value = "${swagger.external_api.products.model.contractTemplateUpdatedAt}", required = true)
    @JsonProperty(required = true)
    @NotNull
    private Instant contractTemplateUpdatedAt;

    @ApiModelProperty(value = "${swagger.external_api.products.model.contractTemplateVersion}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String contractTemplateVersion;

    @ApiModelProperty(value = "${swagger.external_api.products.model.createdAt}", required = true)
    @JsonProperty(required = true)
    @NotNull
    private Instant createdAt;

    @ApiModelProperty(value = "${swagger.external_api.products.model.description}")
    private String description;

    @ApiModelProperty("${swagger.external_api.products.model.urlPublic}")
    private String urlPublic;

    @ApiModelProperty(value = "${swagger.external_api.products.model.urlBO}")
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

    @ApiModelProperty(value = "${swagger.external_api.products.model.roleMappings}")
    @Valid
    private EnumMap<PartyRole, ProductRoleInfo> roleMappings;

    @ApiModelProperty(value = "${swagger.external_api.products.model.roleManagementURL}")
    private String roleManagementURL;



}
