package it.pagopa.selfcare.external_api.model.onboarding;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class PspDataDto {

  @ApiModelProperty(
      value = "${swagger.external_api.institutions.model.pspData.businessRegisterNumber}",
      required = true)
  @JsonProperty(required = true)
  @NotBlank
  private String businessRegisterNumber;

  @ApiModelProperty(
      value = "${swagger.external_api.institutions.model.pspData.legalRegisterName}",
      required = true)
  @JsonProperty(required = true)
  @NotBlank
  private String legalRegisterName;

  @ApiModelProperty(
      value = "${swagger.external_api.institutions.model.pspData.legalRegisterNumber}",
      required = true)
  @JsonProperty(required = true)
  @NotBlank
  private String legalRegisterNumber;

  @ApiModelProperty(
      value = "${swagger.external_api.institutions.model.pspData.abiCode}",
      required = true)
  @JsonProperty(required = true)
  @NotBlank
  private String abiCode;

  @ApiModelProperty(
      value = "${swagger.external_api.institutions.model.pspData.vatNumberGroup}",
      required = true)
  @JsonProperty(required = true)
  @NotNull
  private Boolean vatNumberGroup;

  @ApiModelProperty(value = "${swagger.external_api.institutions.model.pspData.contractType}")
  private String contractType;

  @ApiModelProperty(value = "${swagger.external_api.institutions.model.pspData.contractId}")
  private String contractId;

  @ApiModelProperty(value = "${swagger.external_api.institutions.model.pspData.providerNames}")
  private List<String> providerNames;

  @ApiModelProperty(
      value = "${swagger.external_api.institutions.model.pspData.dpoData}",
      required = true)
  @NotNull
  @Valid
  private DpoDataDto dpoData;
}
