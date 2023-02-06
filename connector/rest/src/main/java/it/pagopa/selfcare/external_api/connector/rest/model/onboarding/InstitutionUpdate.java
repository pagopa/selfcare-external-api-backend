package it.pagopa.selfcare.external_api.connector.rest.model.onboarding;

import it.pagopa.selfcare.external_api.model.onboarding.DataProtectionOfficer;
import it.pagopa.selfcare.external_api.model.onboarding.InstitutionType;
import it.pagopa.selfcare.external_api.model.onboarding.PaymentServiceProvider;
import lombok.Data;

import java.util.List;

@Data
public class InstitutionUpdate {

    private InstitutionType institutionType;
    private String description;
    private String digitalAddress;
    private String address;
    private String taxCode;
    private String zipCode;
    private PaymentServiceProvider paymentServiceProvider;
    private DataProtectionOfficer dataProtectionOfficer;
    private List<String> geographicTaxonomyCodes;

}