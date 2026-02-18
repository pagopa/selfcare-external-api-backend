package it.pagopa.selfcare.external_api.model.onboarding;

import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.external_api.model.user.UserDto;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class OnboardingImportDto {

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.users}")
    @Valid
    private List<UserDto> users;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.importContract}", required = true)
    @NotNull
    @Valid
    private ImportContractDto importContract;

}
