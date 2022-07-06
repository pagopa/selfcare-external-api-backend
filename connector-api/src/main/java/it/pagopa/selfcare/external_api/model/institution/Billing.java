package it.pagopa.selfcare.external_api.model.institution;

import lombok.Data;

@Data
public class Billing {

    private String vatNumber;
    private String recipientCode;
    private boolean publicServices;

}
