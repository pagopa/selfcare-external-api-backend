package it.pagopa.selfcare.external_api.web.model.user;

import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import it.pagopa.selfcare.external_api.model.onboarding.ProductInfo;
import lombok.Data;

import java.util.List;

;

@Data
public class UserInfoResource {

    @ApiModelProperty(value = "${swagger.external_api.userInfo.model.user}")
    private UserResource user;

    @ApiModelProperty(value = "${swagger.external_api.userInfo.model.onboardedInstitutions}")
    private List<OnboardedInstitutionResource> onboardedInstitutions;

    @Data
    public static class OnboardedInstitutionResource {

        @ApiModelProperty(value = "${swagger.external_api.user.model.onboardedInstitutions.id}")
        private String id;
        @ApiModelProperty(value = "${swagger.external_api.user.model.onboardedInstitutions.description}")
        private String description;
        @ApiModelProperty(value = "${swagger.external_api.user.model.onboardedInstitutions.institutionType}")
        private InstitutionType institutionType;
        @ApiModelProperty(value = "${swagger.external_api.user.model.onboardedInstitutions.digitalAddress}")
        private String digitalAddress;
        @ApiModelProperty(value = "${swagger.external_api.user.model.onboardedInstitutions.address}")
        private String address;
        @ApiModelProperty(value = "${swagger.external_api.user.model.onboardedInstitutions.state}")
        private String state;
        @ApiModelProperty(value = "${swagger.external_api.user.model.onboardedInstitutions.zipCode}")
        private String zipCode;
        @ApiModelProperty(value = "${swagger.external_api.user.model.onboardedInstitutions.taxCode}")
        private String taxCode;
        @ApiModelProperty(value = "${swagger.external_api.user.model.onboardedInstitutions.userEmail}")
        private String userEmail;
        @ApiModelProperty(value = "${swagger.external_api.user.model.onboardedInstitutions.productInfo}")
        private ProductInfo productInfo;

    }

}
