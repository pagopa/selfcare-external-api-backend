package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.external_api.model.user.RelationshipState;
import it.pagopa.selfcare.external_api.model.user.UserDetailsWrapper;
import it.pagopa.selfcare.external_api.model.user.UserInfoWrapper;

import java.util.List;
import java.util.Set;

public interface UserService {

    UserInfoWrapper getUserInfo(String fiscalCode, List<RelationshipState> userStatuses);

    UserDetailsWrapper getUserOnboardedProductDetails(String userId, String institutionId, String productId);

}
