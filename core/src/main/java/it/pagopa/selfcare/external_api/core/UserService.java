package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionInfo;
import it.pagopa.selfcare.external_api.model.user.RelationshipState;
import it.pagopa.selfcare.external_api.model.user.UserDetailsWrapper;
import it.pagopa.selfcare.external_api.model.user.UserInfoWrapper;

import java.util.List;

public interface UserService {

    UserInfoWrapper getUserInfo(String fiscalCode, List<RelationshipState> userStatuses);

    UserInfoWrapper getUserInfoV2(String fiscalCode, List<RelationshipState> userStatuses);

    UserDetailsWrapper getUserOnboardedProductDetails(String userId, String institutionId, String productId);

    List<OnboardedInstitutionInfo> getOnboardedInstitutionsDetails(String userId, String productId);

    UserDetailsWrapper getUserOnboardedProductsDetailsV2(String userId, String institutionId, String productId);

    List<OnboardedInstitutionInfo> getOnboardedInstitutionsDetailsActive(String userId, String productId);
}
