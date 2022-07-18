package it.pagopa.selfcare.external_api.web.model.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.external_api.model.groups.UserGroupStatus;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class ProductsResource {
    @ApiModelProperty(value = "${swagger.external_api.products.model.id}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String id;

    @ApiModelProperty(value = "${swagger.external_api.products.model.logo}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String logo;

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

    @ApiModelProperty("${swagger.external_api.products.model.activatedAt}")
    private OffsetDateTime activatedAt;

    @ApiModelProperty(value = "${swagger.external_api.products.model.authorized}", required = true)
    @JsonProperty(required = true)
    private boolean authorized;

    @ApiModelProperty(value = "${swagger.external_api.model.userRole}")
    private String userRole;

    @ApiModelProperty(value = "${swagger.external_api.products.model.status}", required = true)
    @JsonProperty(required = true)
    @NotNull
    private UserGroupStatus status;

    @ApiModelProperty(value = "${swagger.external_api.products.model.children}")
    private List<SubProductResource> children;

}
