package it.pagopa.selfcare.external_api.web.model.institutions;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.external_api.model.onboarding.InstitutionType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
public class InstitutionResource {

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

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.userRole}", required = true)
    @JsonProperty(required = true)
    @NotNull
    private SelfCareAuthority userRole;
}
