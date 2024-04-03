package it.pagopa.selfcare.external_api.api;

import it.pagopa.selfcare.external_api.model.user.User;
import it.pagopa.selfcare.external_api.model.user.UserInstitutions;

import java.util.List;


public interface UserMsConnector {

     List<UserInstitutions> getUsersInstitutions(String userId);

     User searchUserByExternalId(String fiscalCode);

}
