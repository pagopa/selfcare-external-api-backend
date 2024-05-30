package it.pagopa.selfcare.external_api.web.model.national_registries;

import lombok.Data;

@Data
public class VerifyRequestDto {

    private String taxId;
    private String vatNumber;
}
