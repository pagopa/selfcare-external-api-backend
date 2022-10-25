package it.pagopa.selfcare.external_api.connector.rest;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.external_api.api.UserRegistryConnector;
import it.pagopa.selfcare.external_api.connector.rest.client.UserRegistryRestClient;
import it.pagopa.selfcare.external_api.model.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.EnumSet;
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

}
