package it.pagopa.selfcare.external_api.api;

import it.pagopa.selfcare.external_api.model.user.User;
import it.pagopa.selfcare.external_api.model.user.UserInstitution;

import java.util.List;


public interface UserMsConnector {

     List<UserInstitution> getUsersInstitutions(String userId, List<String> product);

     User searchUserByExternalId(String fiscalCode);

}
