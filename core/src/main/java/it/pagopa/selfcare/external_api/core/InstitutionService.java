package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.external_api.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.external_api.model.product.Product;

import java.util.Collection;
import java.util.List;

public interface InstitutionService {

    Collection<InstitutionInfo> getInstitutions(String productId);

    List<Product> getInstitutionUserProducts(String institutionId);

}
