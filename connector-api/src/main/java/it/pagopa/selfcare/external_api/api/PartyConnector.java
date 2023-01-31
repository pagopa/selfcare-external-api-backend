package it.pagopa.selfcare.external_api.api;

import it.pagopa.selfcare.external_api.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.external_api.model.onboarding.Institution;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingImportData;
import it.pagopa.selfcare.external_api.model.product.PartyProduct;
import it.pagopa.selfcare.external_api.model.user.UserInfo;

import java.util.Collection;
import java.util.List;

public interface PartyConnector {

    Collection<InstitutionInfo> getOnBoardedInstitutions(String productId);

    List<PartyProduct> getInstitutionUserProducts(String institutionId, String userId);

    Collection<UserInfo> getUsers(UserInfo.UserInfoFilter userInfoFilter);

    void verifyOnboarding(String externalInstitutionId, String productId);

    Institution getInstitutionByExternalId(String externalInstitutionId);

    Institution createInstitutionUsingExternalId(String institutionExternalId);

    void oldContractOnboardingOrganization(OnboardingImportData onboardingImportData);
}
