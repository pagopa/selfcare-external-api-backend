package it.pagopa.selfcare.external_api.model.institutions;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.external_api.model.onboarding.Billing;
import it.pagopa.selfcare.external_api.model.onboarding.InstitutionType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode
public class InstitutionInfo {

    private String id;
    private String externalId;
    private String description;
    private String status;
    private String taxCode;
    private String address;
    private String digitalAddress;
    private String pricingPlan;
    private String zipCode;
    private String category;
    private Billing billing;
    private String origin;
    private String originId;
    private InstitutionType institutionType;
    private PartyRole userRole;
    private List<String> productRoles;

}
