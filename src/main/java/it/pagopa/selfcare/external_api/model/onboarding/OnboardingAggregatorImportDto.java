package it.pagopa.selfcare.external_api.model.onboarding;

import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.external_api.model.user.UserDto;
import it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.Aggregate;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OnboardingAggregatorImportDto {

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.users}", required = true)
    @NotEmpty
    @Valid
    private List<UserDto> users;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.importContract}", required = true)
    @NotNull
    @Valid
    private ImportContractDto importContract;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.aggregates}", required = true)
    @NotEmpty
    @Valid
    private List<Aggregate> aggregates;

}
