package it.pagopa.selfcare.external_api.model.product;

import io.swagger.v3.oas.annotations.media.Schema;
import it.pagopa.selfcare.onboarding.common.PartyRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.Instant;
import java.util.EnumMap;

@Data
public class ProductResource {
  @Schema(description = "${swagger.external_api.products.model.id}", required = true)
  @NotBlank
  private String id;

  @Schema(description = "${swagger.external_api.products.model.title}", required = true)
  @NotBlank
  private String title;

  @Schema(description = "${swagger.external_api.products.model.contractTemplatePath}", required = true)
  @NotBlank
  private String contractTemplatePath;

  @Schema(description = "${swagger.external_api.products.model.contractTemplateVersion}", required = true)
  @NotBlank
  private String contractTemplateVersion;

  @Schema(description = "${swagger.external_api.products.model.createdAt}")
  private Instant createdAt;

  @Schema(description = "${swagger.external_api.products.model.description}", required = true)
  @NotBlank
  private String description;

  @Schema(description = "${swagger.external_api.products.model.urlPublic}")
  private String urlPublic;

  @Schema(description = "${swagger.external_api.products.model.urlBO}", required = true)
  @NotBlank
  private String urlBO;

  @Schema(description = "${swagger.external_api.products.model.depictImageUrl}")
  private String depictImageUrl;

  @Schema(description = "${swagger.external_api.products.model.identityTokenAudience}")
  private String identityTokenAudience;

  @Schema(description = "${swagger.external_api.products.model.logo}")
  private String logo;

  @Schema(description = "${swagger.external_api.products.model.logoBgColor}")
  private String logoBgColor;

  @Schema(description = "${swagger.external_api.products.model.parentId}")
  private String parentId;

  @Schema(description = "${swagger.external_api.products.model.roleMappings}", required = true)
  @NotEmpty
  private EnumMap<PartyRole, it.pagopa.selfcare.product.entity.ProductRoleInfo> roleMappings;

  @Schema(description = "${swagger.external_api.products.model.roleManagementURL}")
  private String roleManagementURL;
}
