package it.pagopa.selfcare.external_api.model.onboarding;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.external_api.model.institutions.Attribute;
import it.pagopa.selfcare.external_api.model.product.ProductInfo;
import it.pagopa.selfcare.external_api.model.user.RelationshipState;
import lombok.Data;

import java.util.List;

@Data
public class OnboardingResponseData {
    private String id;
    private String externalId;
    private String description;
    private String taxCode;
    private String digitalAddress;
    private String address;
    private String zipCode;
    private RelationshipState state;
    private PartyRole role;
    private ProductInfo productInfo;
    private InstitutionType institutionType;
    private String pricingPlan;
    private Billing billing;
    private String origin;
    private String originId;
    private List<Attribute> attributes;
    private String rea;
    private String shareCapital;
    private String businessRegisterPlace;
    private String supportEmail;
    private String supportPhone;
}
