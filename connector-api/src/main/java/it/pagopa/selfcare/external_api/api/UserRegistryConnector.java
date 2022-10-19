package it.pagopa.selfcare.external_api.api;

import it.pagopa.selfcare.external_api.model.user.User;

import java.util.EnumSet;

public interface UserRegistryConnector {

    User getUserByInternalId(String userId, EnumSet<User.Fields> fieldList);

}
