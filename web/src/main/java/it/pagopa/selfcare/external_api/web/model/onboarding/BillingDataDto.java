package it.pagopa.selfcare.external_api.web.model.onboarding;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class BillingDataDto {

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.name}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String businessName;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.address}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String registeredOffice;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.digitalAddress}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String digitalAddress;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.zipCode}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String zipCode;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.taxCode}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String taxCode;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.vatNumber}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String vatNumber;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.recipientCode}", required = true)
    private String recipientCode;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.publicServices}")
    private Boolean publicServices;

}
