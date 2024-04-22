package it.pagopa.selfcare.external_api.web.model.institutions;

import lombok.Data;

@Data
public class BillingResponse {
    private String vatNumber;
    private String recipientCode;
    private boolean publicServices;
}
