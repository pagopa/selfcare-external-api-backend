package it.pagopa.selfcare.external_api.connector.api;

import it.pagopa.selfcare.external_api.connector.model.groups.UserGroupFilter;
import it.pagopa.selfcare.external_api.connector.model.groups.UserGroupInfo;
import org.springframework.data.domain.Pageable;

import java.util.Collection;

public interface UserGroupConnector {

    Collection<UserGroupInfo> getUserGroups(UserGroupFilter filter, Pageable pageable);

}
