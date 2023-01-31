package it.pagopa.selfcare.external_api.web.model.onboarding;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ImportContractDto {

    @ApiModelProperty(value = "${swagger.external_api.importContract.model.fileName}")
    @NotBlank
    private String fileName;

    @ApiModelProperty(value = "${swagger.external_api.importContract.model.filePath}")
    @NotBlank
    private String filePath;

    @ApiModelProperty(value = "${swagger.external_api.importContract.model.contractType}")
    @NotBlank
    private String contractType;

}
