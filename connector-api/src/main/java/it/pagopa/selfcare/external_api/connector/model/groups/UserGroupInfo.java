package it.pagopa.selfcare.external_api.connector.model.groups;

import it.pagopa.selfcare.external_api.connector.model.user.User;
import it.pagopa.selfcare.external_api.connector.model.user.UserInfo;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class UserGroupInfo {
    private String id;
    private String institutionId;
    private String productId;
    private String description;
    private String name;
    private UserGroupStatus status;
    private List<UserInfo> members;
    private Instant createdAt;
    private User createdBy;
    private Instant modifiedAt;
    private User modifiedBy;
}
