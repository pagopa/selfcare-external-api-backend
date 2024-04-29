package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.external_api.model.institutions.GeographicTaxonomy;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.external_api.model.institutions.SearchMode;
import it.pagopa.selfcare.external_api.model.pnpg.CreatePnPgInstitution;
import it.pagopa.selfcare.external_api.model.product.Product;
import it.pagopa.selfcare.external_api.model.user.UserInfo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface InstitutionService {

    Collection<InstitutionInfo> getInstitutions(String productId);

    List<Product> getInstitutionUserProducts(String institutionId);

    List<Product> getInstitutionUserProductsV2(String institutionId);

    Collection<UserInfo> getInstitutionProductUsers(String institutionId, String productId, Optional<String> userId, Optional<Set<String>> productRoles, String xSelfCareUid);

    Collection<UserInfo> getInstitutionProductUsersV2(String institutionId, String productId, String userId, Optional<Set<String>> productRoles, String xSelfCareUid);

    List<GeographicTaxonomy> getGeographicTaxonomyList(String institutionId);

    Collection<Institution> getInstitutionsByGeoTaxonomies(Set<String> geoTaxIds, SearchMode searchMode);

    String addInstitution(CreatePnPgInstitution request);
}
