package it.pagopa.selfcare.external_api.model.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

;

@Data
public class UserInstitutionResource {

    @ApiModelProperty(value = "${swagger.external_api.userInstitution.model.id}")
    private String id;

    @ApiModelProperty(value = "${swagger.external_api.user.model.id}", required = true)
    private String userId;
    @ApiModelProperty(value = "${swagger.external_api.institutions.model.id}")
    private String institutionId;
    @ApiModelProperty(value = "${swagger.external_api.user.model.onboardedInstitutions.description}")
    private String institutionDescription;
    @ApiModelProperty(value = "${swagger.external_api.institutions.model.rootName}")
    private String institutionRootName;
    @ApiModelProperty(value = "${swagger.external_api.userInfo.model.onboardedInstitutions}")
    private List<UserProductResource> products;

    @Data
    public static class UserProductResource {
        @ApiModelProperty(value = "${swagger.external_api.api.tokens.productId}")
        private String productId;
        @ApiModelProperty(value = "${swagger.external_api.api.tokens.id}")
        private String tokenId;
        @ApiModelProperty(value = "${swagger.external_api.model.states}")
        private String status;
        @ApiModelProperty(value = "${swagger.external_api.tokens.model.productRole}")
        private String productRole;
        @ApiModelProperty(value = "${swagger.external_api.tokens.model.productRoleLabel}")
        private String productRoleLabel;
        @ApiModelProperty(value = "Available values: MANAGER, DELEGATE, SUB_DELEGATE, OPERATOR, ADMIN_EA")
        private String role;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

    }

}
