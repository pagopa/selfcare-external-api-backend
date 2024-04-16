package it.pagopa.selfcare.external_api.web.model.onboarding;

import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import it.pagopa.selfcare.external_api.web.model.user.UserDto;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

;

@Data
public class OnboardingDto {

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.users}", required = true)
    @NotEmpty
    @Valid
    private List<UserDto> users;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.billingData}", required = true)
    @NotNull
    @Valid
    private BillingDataDto billingData;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.institutionType}", required = true)
    @NotNull
    private InstitutionType institutionType;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.origin}")
    private String origin;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.pricingPlan}")
    private String pricingPlan;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.pspData}")
    @Valid
    private PspDataDto pspData;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.geographicTaxonomy}", required = true)
    @NotNull
    @Valid
    private List<GeographicTaxonomyDto> geographicTaxonomies;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.companyInformations}")
    @Valid
    private CompanyInformationsDto companyInformations;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.assistance}")
    @Valid
    private AssistanceContactsDto assistanceContacts;
}
