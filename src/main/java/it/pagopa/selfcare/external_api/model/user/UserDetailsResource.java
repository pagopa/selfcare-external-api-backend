package it.pagopa.selfcare.external_api.model.user;

import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedProductResource;
import lombok.Data;

@Data
public class UserDetailsResource {
    @ApiModelProperty(value = "${swagger.external_api.user.model.id}")
    private String id;
    @ApiModelProperty(value =" swagger.external_api.institutions.model.id")
    private String institutionId;
    @ApiModelProperty(value = "${swagger.external_api.product.onboardedProduct}")
    private OnboardedProductResource onboardedProductDetails;
}
