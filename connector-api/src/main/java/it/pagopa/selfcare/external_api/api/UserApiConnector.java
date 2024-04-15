package it.pagopa.selfcare.external_api.api;

import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.user.UserToOnboard;

import java.util.List;


public interface UserApiConnector {

    String createUser(Institution institution, String productId, String role, List<String> productRoles, UserToOnboard user);

    String addUserRole(String userId, Institution institution, String productId, String role, List<String> productRoles);
}
