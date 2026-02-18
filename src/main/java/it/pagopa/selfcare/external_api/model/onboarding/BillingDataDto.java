package it.pagopa.selfcare.external_api.model.onboarding;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

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

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.zipCode}")
    private String zipCode;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.taxCode}")
    private String taxCode;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.taxCodeInvoicing}")
    private String taxCodeInvoicing;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.vatNumber}")
    private String vatNumber;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.recipientCode}")
    private String recipientCode;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.publicServices}")
    private Boolean publicServices;

}
