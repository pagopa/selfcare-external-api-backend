package it.pagopa.selfcare.external_api.api;

import it.pagopa.selfcare.external_api.model.institutions.GeographicTaxonomy;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.institutions.SearchMode;
import it.pagopa.selfcare.external_api.model.onboarding.InstitutionOnboarding;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionInfo;
import it.pagopa.selfcare.external_api.model.pnpg.CreatePnPgInstitution;

import java.util.Collection;
import java.util.List;

public interface MsCoreConnector {

    String createPnPgInstitution(CreatePnPgInstitution request);


    InstitutionOnboarding getInstitutionOnboardings(String institutionId, String productId);

    Institution getInstitutionByExternalId(String externalInstitutionId);

    List<OnboardedInstitutionInfo> getInstitutionDetails(String institutionId);

    List<GeographicTaxonomy> getGeographicTaxonomyList(String institutionId);

    Collection<Institution> getInstitutionsByGeoTaxonomies(String geoTaxIds, SearchMode searchMode);

    List<String> getInstitutionUserProductsV2(String institutionId, String id);

    String createPgInstitution(String description, String taxId);

}
