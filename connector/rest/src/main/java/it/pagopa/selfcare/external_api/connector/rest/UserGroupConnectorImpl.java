package it.pagopa.selfcare.external_api.connector.rest;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.external_api.connector.api.UserGroupConnector;
import it.pagopa.selfcare.external_api.connector.model.groups.UserGroupFilter;
import it.pagopa.selfcare.external_api.connector.model.groups.UserGroupInfo;
import it.pagopa.selfcare.external_api.connector.model.user.User;
import it.pagopa.selfcare.external_api.connector.model.user.UserInfo;
import it.pagopa.selfcare.external_api.connector.rest.client.UserGroupRestClient;
import it.pagopa.selfcare.external_api.connector.rest.model.UserGroupResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserGroupConnectorImpl implements UserGroupConnector {

    private final UserGroupRestClient restClient;

    static final Function<UserGroupResponse, UserGroupInfo> GROUP_RESPONSE_TO_GROUP_INFO = groupResponse -> {
        UserGroupInfo groupInfo = new UserGroupInfo();
        groupInfo.setId(groupResponse.getId());
        groupInfo.setInstitutionId(groupResponse.getInstitutionId());
        groupInfo.setProductId(groupResponse.getProductId());
        groupInfo.setName(groupResponse.getName());
        groupInfo.setDescription(groupResponse.getDescription());
        groupInfo.setStatus(groupResponse.getStatus());
        if (groupResponse.getMembers() != null) {
            List<UserInfo> members = groupResponse.getMembers().stream().map(id -> {
                UserInfo member = new UserInfo();
                member.setId(id);
                return member;
            }).collect(Collectors.toList());
            groupInfo.setMembers(members);
        }
        groupInfo.setCreatedAt(groupResponse.getCreatedAt());
        groupInfo.setModifiedAt(groupResponse.getModifiedAt());
        User createdBy = new User();
        createdBy.setId(groupResponse.getCreatedBy());
        groupInfo.setCreatedBy(createdBy);

        if (groupResponse.getModifiedBy() != null) {
            User userInfo = new User();
            userInfo.setId(groupResponse.getModifiedBy());
            groupInfo.setModifiedBy(userInfo);
        }
        return groupInfo;

    };

    @Autowired
    public UserGroupConnectorImpl(UserGroupRestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Collection<UserGroupInfo> getUserGroups(UserGroupFilter filter, Pageable pageable) {
        log.trace("getUserGroups start");
        log.debug("getUserGroups institutionId = {}, productId = {}, userId = {}, pageable = {}", filter.getInstitutionId(), filter.getProductId(), filter.getUserId(), pageable);
        Collection<UserGroupInfo> groupInfos = Collections.emptyList();
        List<UserGroupResponse> userGroups = restClient.getUserGroups(filter.getInstitutionId().orElse(null), filter.getProductId().orElse(null), filter.getUserId().orElse(null), pageable);
        if (userGroups != null) {
            groupInfos = userGroups.stream().map(GROUP_RESPONSE_TO_GROUP_INFO).collect(Collectors.toList());
        }
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUserGroups result = {}", userGroups);
        log.trace("getUserGroups end");
        return groupInfos;
    }
}
