package it.pagopa.selfcare.external_api.connector.rest;

import feign.FeignException;
import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.external_api.api.UserRegistryConnector;
import it.pagopa.selfcare.external_api.connector.rest.client.UserRegistryRestClient;
import it.pagopa.selfcare.external_api.connector.rest.model.user_registry.EmbeddedExternalId;
import it.pagopa.selfcare.external_api.model.user.MutableUserFieldsDto;
import it.pagopa.selfcare.external_api.model.user.SaveUserDto;
import it.pagopa.selfcare.external_api.model.user.User;
import it.pagopa.selfcare.external_api.model.user.UserId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserRegistryConnectorImpl implements UserRegistryConnector {

    private final UserRegistryRestClient restClient;

    @Autowired
    public UserRegistryConnectorImpl(UserRegistryRestClient restClient) {
        this.restClient = restClient;
    }


    @Override
    public User getUserByInternalId(String userId, EnumSet<User.Fields> fieldList) {
        log.trace("getUserByInternalId start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUserByInternalId userId = {}", userId);
        Assert.hasText(userId, "A userId is required");
        Assert.notEmpty(fieldList, "At least one user fields is required");
        User result = restClient.getUserByInternalId(UUID.fromString(userId), fieldList);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUserByInternalId result = {}", result);
        log.trace("getUserByInternalId end");
        return result;
    }

    @Override
    public Optional<User> search(String externalId, EnumSet<User.Fields> fieldList) {
        log.trace("getUserByExternalId start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUserByExternalId externalId = {}", externalId);
        Assert.hasText(externalId, "A TaxCode is required");
        Assert.notEmpty(fieldList, "At least one user fields is required");
        Optional<User> user;
        try {
            user = Optional.of(restClient.search(new EmbeddedExternalId(externalId), fieldList));
        } catch (FeignException.NotFound e) {
            user = Optional.empty();
        }
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUserByExternalId result = {}", user);
        log.trace("getUserByExternalId end");

        return user;
    }

    @Override
    public void updateUser(UUID id, MutableUserFieldsDto userDto) {
        log.trace("update start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "update id = {}, userDto = {}}", id, userDto);
        Assert.notNull(id, "A UUID is required");
        restClient.patchUser(id, userDto);
        log.trace("update end");
    }


    @Override
    public UserId saveUser(SaveUserDto dto) {
        log.trace("saveUser start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "saveUser dto = {}}", dto);
        UserId userId = restClient.saveUser(dto);
        log.debug("saveUser result = {}", userId);
        log.trace("saveUser end");
        return userId;
    }

}
