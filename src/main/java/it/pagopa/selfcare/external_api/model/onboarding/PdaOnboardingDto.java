package it.pagopa.selfcare.external_api.model.onboarding;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.external_api.model.user.UserDto;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class PdaOnboardingDto {

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.users}", required = true)
    @NotEmpty
    @Valid
    private List<UserDto> users;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.name}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String businessName;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.taxCode}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String taxCode;

    @ApiModelProperty(value = "${swagger.external_api.product.model.id}", required = true)
    @NotNull
    private String productId;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.vatNumber}", required = true)
    private String vatNumber;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.recipientCode}", required = true)
    private String recipientCode;
}
