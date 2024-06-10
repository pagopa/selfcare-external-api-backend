package it.pagopa.selfcare.external_api.web.model.user;

import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import it.pagopa.selfcare.external_api.model.onboarding.ProductInfo;
import it.pagopa.selfcare.external_api.model.user.OnboardedProductResponse;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

;

@Data
public class UserInstitutionResource {

    private String id;

    @ApiModelProperty(value = "${swagger.external_api.user.model.id}", required = true)
    private String userId;
    @ApiModelProperty(value = "${swagger.external_api.user.model.onboardedInstitutions.id}")
    private String institutionId;
    @ApiModelProperty(value = "${swagger.external_api.user.model.onboardedInstitutions.description}")
    private String institutionDescription;
    private String institutionRootName;
    @ApiModelProperty(value = "${swagger.external_api.userInfo.model.onboardedInstitutions}")
    private List<UserProductResource> products;

    @Data
    public static class UserProductResource {
        private String productId;
        private String tokenId;
        private String status;
        private String productRole;
        @ApiModelProperty(value = "Available values: MANAGER, DELEGATE, SUB_DELEGATE, OPERATOR, ADMIN_EA")
        private String role;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

    }

}
