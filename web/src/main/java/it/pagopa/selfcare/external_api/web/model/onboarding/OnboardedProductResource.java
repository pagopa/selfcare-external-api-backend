package it.pagopa.selfcare.external_api.web.model.onboarding;

import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.commons.base.security.PartyRole;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class OnboardedProductResource {
    @ApiModelProperty(value = "${swagger.external_api.products.model.id}")
    private String productId;
    @ApiModelProperty(value = "${swagger.external_api.user.model.role}")
    private PartyRole role;
    @ApiModelProperty(value = "${swagger.external_api.user.model.productRoles}")
    private List<String> roles;
    @ApiModelProperty(value = "${swagger.external_api.onboarding.model.createdAt}")
    private OffsetDateTime createdAt;
}
