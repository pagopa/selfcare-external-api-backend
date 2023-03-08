package it.pagopa.selfcare.external_api.web.model.onboarding;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
    @Valid
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime onboardingDate;

}
