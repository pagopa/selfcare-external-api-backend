package it.pagopa.selfcare.external_api.model.onboarding;

import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.external_api.model.institution.GPUData;
import it.pagopa.selfcare.external_api.model.institution.InstitutionLocationDataDto;
import it.pagopa.selfcare.external_api.model.user.UserDto;
import it.pagopa.selfcare.onboarding.common.InstitutionType;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OnboardingProductDto {

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.users}", required = true)
    @NotEmpty
    @Valid
    private List<UserDto> users;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.billingData}")
    @Valid
    private BillingDataDto billingData;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.locationData}")
    private InstitutionLocationDataDto institutionLocationData;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.institutionType}", required = true)
    @NotNull
    private InstitutionType institutionType;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.origin}")
    private String origin;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.originId}")
    private String originId;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.pricingPlan}")
    private String pricingPlan;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.pspData}")
    @Valid
    private PspDataDto pspData;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.gpuData}")
    @Valid
    private GPUData gpuData;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.geographicTaxonomies}", required = true)
    @NotNull
    @Valid
    private List<GeographicTaxonomyDto> geographicTaxonomies;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.companyInformations}")
    @Valid
    private CompanyInformationsDto companyInformations;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.assistance}")
    @Valid
    private AssistanceContactsDto assistanceContacts;

    @ApiModelProperty(value = "${swagger.external_api.product.model.id}", required = true)
    @NotNull
    private String productId;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.taxCode}")
    private String taxCode;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.subUnitCode}")
    private String subunitCode;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.subUnitType}")
    private String subunitType;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.additionalInformations}")
    private AdditionalInformationsDto additionalInformations;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.isAggregator}")
    private Boolean isAggregator;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.istatCode}")
    private String istatCode;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.aggregates}")
    @Valid
    private List<AggregateInstitutionDto> aggregates;
}
