package it.pagopa.selfcare.external_api.api;

import it.pagopa.selfcare.external_api.model.institutions.GeographicTaxonomy;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.external_api.model.institutions.SearchMode;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingData;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingImportData;
import it.pagopa.selfcare.external_api.model.product.PartyProduct;
import it.pagopa.selfcare.external_api.model.relationship.Relationships;
import it.pagopa.selfcare.external_api.model.user.UserInfo;
import org.springframework.http.ResponseEntity;

import java.util.Collection;
import java.util.List;

public interface PartyConnector {

    Collection<InstitutionInfo> getOnBoardedInstitutions(String productId);

    List<PartyProduct> getInstitutionUserProducts(String institutionId, String userId);

    Collection<UserInfo> getUsers(UserInfo.UserInfoFilter userInfoFilter);

    ResponseEntity<Void> verifyOnboarding(String externalInstitutionId, String productId);

    ResponseEntity<Void> verifyOnboarding(String taxCode, String subunitCode, String productId);

    Institution getInstitutionByExternalId(String externalInstitutionId);

    List<Institution> getInstitutionsByTaxCodeAndSubunitCode(String taxCode, String subunitCode);

    Institution createInstitutionUsingExternalId(String institutionExternalId);

    Institution createInstitutionFromIpa(String taxCode, String subunitCode, String subunitType);

    Institution createInstitutionRaw(OnboardingData onboardingData);

    void oldContractOnboardingOrganization(OnboardingImportData onboardingImportData);

    List<GeographicTaxonomy> getGeographicTaxonomyList(String institutionId);

    Collection<Institution> getInstitutionsByGeoTaxonomies(String geoTaxIds, SearchMode searchMode);

    void autoApprovalOnboarding(OnboardingData onboardingData);

    Relationships getRelationships(String institutionExternalId);
}
