package it.pagopa.selfcare.external_api.api;

import it.pagopa.selfcare.external_api.model.institutions.*;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingData;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingImportData;
import it.pagopa.selfcare.external_api.model.product.PartyProduct;
import it.pagopa.selfcare.external_api.model.user.UserInfo;
import org.springframework.http.ResponseEntity;

import java.util.Collection;
import java.util.List;

public interface PartyConnector {

    Collection<InstitutionInfo> getOnBoardedInstitutions(String productId);

    List<PartyProduct> getInstitutionUserProducts(String institutionId, String userId);

    Collection<UserInfo> getUsers(UserInfo.UserInfoFilter userInfoFilter);

    ResponseEntity<Void> verifyOnboarding(String externalInstitutionId, String productId);

    Institution getInstitutionByExternalId(String externalInstitutionId);

    Institution createInstitutionUsingExternalId(String institutionExternalId);

    Institution createInstitutionRaw(OnboardingData onboardingData);

    void oldContractOnboardingOrganization(OnboardingImportData onboardingImportData);

    List<GeographicTaxonomy> getGeographicTaxonomyList(String institutionId);

    Collection<Institution> getInstitutionsByGeoTaxonomies(String geoTaxIds, SearchMode searchMode);

    void autoApprovalOnboarding(OnboardingData onboardingData);

    RelationshipsResponse getRelationships(String institutionExternalId);
}
