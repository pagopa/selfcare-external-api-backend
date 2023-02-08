package it.pagopa.selfcare.external_api.api;

import it.pagopa.selfcare.external_api.model.user.MutableUserFieldsDto;
import it.pagopa.selfcare.external_api.model.user.SaveUserDto;
import it.pagopa.selfcare.external_api.model.user.User;
import it.pagopa.selfcare.external_api.model.user.UserId;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

public interface UserRegistryConnector {

    User getUserByInternalId(String userId, EnumSet<User.Fields> fieldList);

    Optional<User> search(String externalId, EnumSet<User.Fields> fieldList);

    void updateUser(UUID id, MutableUserFieldsDto entity);

    UserId saveUser(SaveUserDto entity);

}
