package it.pagopa.selfcare.external_api.model.onboarding;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Data
public class ImportContractDto {

    @ApiModelProperty(value = "${swagger.external_api.importContract.model.fileName}", required = true)
    @NotBlank
    private String fileName;

    @ApiModelProperty(value = "${swagger.external_api.importContract.model.filePath}", required = true)
    @NotBlank
    private String filePath;

    @ApiModelProperty(value = "${swagger.external_api.importContract.model.contractType}", required = true)
    @NotBlank
    private String contractType;

    @ApiModelProperty(value = "${swagger.external_api.institutions.model.onboardingDate}", required = true)
    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime onboardingDate;

}
