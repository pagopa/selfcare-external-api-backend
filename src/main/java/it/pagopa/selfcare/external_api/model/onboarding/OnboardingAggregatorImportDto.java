package it.pagopa.selfcare.external_api.model.onboarding;

import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.external_api.model.user.UserDto;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OnboardingAggregatorImportDto {

  @ApiModelProperty(value = "${swagger.external_api.institutions.model.users}")
  @Valid
  private List<UserDto> users;

  @ApiModelProperty(
      value = "${swagger.external_api.institutions.model.importContract}",
      required = true)
  @NotNull
  @Valid
  private ImportContractDto importContract;

  @ApiModelProperty(
      value = "${swagger.external_api.institutions.model.aggregates}",
      required = true)
  @NotEmpty
  @Valid
  private List<TaxCodeDto> aggregates;
}
