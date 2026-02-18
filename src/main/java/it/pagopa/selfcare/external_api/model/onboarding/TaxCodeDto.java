package it.pagopa.selfcare.external_api.model.onboarding;


import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class TaxCodeDto {

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.aggregates.taxCode}", required = true)
    @NotEmpty
    @Valid
    String taxCode;
}
