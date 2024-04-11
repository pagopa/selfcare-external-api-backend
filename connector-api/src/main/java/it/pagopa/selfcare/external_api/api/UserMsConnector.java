package it.pagopa.selfcare.external_api.api;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.external_api.model.user.User;
import it.pagopa.selfcare.external_api.model.user.UserInstitution;

import java.util.List;


public interface UserMsConnector {

     List<UserInstitution> getUsersInstitutions(String userId, String institutionId, Integer page, Integer size, List<String> productRoles, List<String> products, List<PartyRole> roles, List<String> states);

     User searchUserByExternalId(String fiscalCode);

}
