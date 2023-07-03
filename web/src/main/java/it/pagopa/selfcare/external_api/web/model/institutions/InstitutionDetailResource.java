package it.pagopa.selfcare.external_api.web.model.institutions;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.external_api.model.onboarding.InstitutionType;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data
public class InstitutionDetailResource {

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.id}", required = true)
    private UUID id;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.name}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String description;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.externalId}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String externalId;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.originId}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String originId;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.institutionType}", required = true)
    private InstitutionType institutionType;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.digitalAddress}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String digitalAddress;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.address}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String address;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.zipCode}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String zipCode;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.taxCode}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String taxCode;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.origin}", required = true)
    private String origin;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.geographicTaxonomy}", required = true)
    @JsonProperty(required = true)
    @NotNull
    private List<GeographicTaxonomyResource> geographicTaxonomies;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.rea}")
    private String rea;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.shareCapital}")
    private String shareCapital;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.businessRegisterPlace}")
    private String businessRegisterPlace;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.supportEmail}")
    @Email
    private String supportEmail;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.supportPhone}")
    private String supportPhone;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.imported}")
    private Boolean imported;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.subUnitCode}")
    private String subunitCode;
    @ApiModelProperty(value = "${swagger.external_api.institutions.model.subUnitType}")
    private String subunitType;
    @ApiModelProperty(value = "${swagger.external_api.institutions.model.parentDescription}")
    private String parentDescription;
    @ApiModelProperty(value = "${swagger.external_api.institutions.model.aooParentCode}")
    private String aooParentCode;
}
