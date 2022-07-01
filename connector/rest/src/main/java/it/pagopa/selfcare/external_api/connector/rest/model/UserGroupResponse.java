package it.pagopa.selfcare.external_api.connector.rest.model;

import it.pagopa.selfcare.external_api.connector.model.groups.UserGroupStatus;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class UserGroupResponse {

    private String id;
    private String institutionId;
    private String productId;
    private String name;
    private String description;
    private UserGroupStatus status;
    private List<String> members;
    private Instant createdAt;
    private String createdBy;
    private Instant modifiedAt;
    private String modifiedBy;

}
