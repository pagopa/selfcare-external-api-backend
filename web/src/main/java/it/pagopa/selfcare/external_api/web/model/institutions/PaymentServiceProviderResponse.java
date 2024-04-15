package it.pagopa.selfcare.external_api.web.model.institutions;

import lombok.Data;

@Data
public class PaymentServiceProviderResponse {
    private String abiCode;
    private String businessRegisterNumber;
    private String legalRegisterNumber;
    private String legalRegisterName;
    private boolean vatNumberGroup;
}
