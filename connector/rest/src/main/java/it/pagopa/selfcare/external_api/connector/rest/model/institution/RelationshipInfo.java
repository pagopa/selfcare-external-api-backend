package it.pagopa.selfcare.external_api.connector.rest.model.institution;

import it.pagopa.selfcare.external_api.connector.rest.model.PartyRole;
import it.pagopa.selfcare.external_api.connector.rest.model.product.ProductInfo;
import it.pagopa.selfcare.external_api.model.institution.Billing;
import it.pagopa.selfcare.external_api.model.user.RelationshipState;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class RelationshipInfo {

    private String id;
    private String from;
    private String to;
    private PartyRole role;
    private ProductInfo product;
    private RelationshipState state;
    private String pricingPlan;
    private InstitutionUpdate institutionUpdate;
    private Billing billing;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
