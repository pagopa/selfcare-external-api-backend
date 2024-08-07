package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.external_api.model.institutions.GeographicTaxonomy;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.institutions.SearchMode;
import it.pagopa.selfcare.external_api.model.nationalRegistries.LegalVerification;
import it.pagopa.selfcare.external_api.model.pnpg.CreatePnPgInstitution;
import it.pagopa.selfcare.external_api.model.user.UserProductResponse;
import it.pagopa.selfcare.product.entity.Product;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface InstitutionService {


    List<Product> getInstitutionUserProductsV2(String institutionId, String userId);

    Collection<UserProductResponse> getInstitutionProductUsersV2(String institutionId, String productId, String userId, Set<String> productRoles, String xSelfCareUid);

    List<GeographicTaxonomy> getGeographicTaxonomyList(String institutionId);

    Collection<Institution> getInstitutionsByGeoTaxonomies(Set<String> geoTaxIds, SearchMode searchMode);

    String addInstitution(CreatePnPgInstitution request);

    LegalVerification verifyLegal(String taxId, String vatNumber);
}
