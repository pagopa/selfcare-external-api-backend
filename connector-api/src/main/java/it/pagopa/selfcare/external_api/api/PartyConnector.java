package it.pagopa.selfcare.external_api.api;

import it.pagopa.selfcare.external_api.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.external_api.model.product.PartyProduct;

import java.util.Collection;
import java.util.List;

public interface PartyConnector {

    Collection<InstitutionInfo> getOnBoardedInstitutions(String productId);

    List<PartyProduct> getInstitutionUserProducts(String productId, String institutionId, String userId);

}
