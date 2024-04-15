package it.pagopa.selfcare.external_api.api;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.user.User;
import it.pagopa.selfcare.external_api.model.user.UserInstitution;
import it.pagopa.selfcare.external_api.model.user.UserToOnboard;

import java.util.List;


public interface UserMsConnector {

    String createUser(Institution institution, String productId, String role, List<String> productRoles, UserToOnboard user);

    String addUserRole(String userId, Institution institution, String productId, String role, List<String> productRoles);

    List<UserInstitution> getUsersInstitutions(String userId, String institutionId, Integer page, Integer size, List<String> productRoles, List<String> products, List<PartyRole> roles, List<String> states);

    User searchUserByExternalId(String fiscalCode);
}
