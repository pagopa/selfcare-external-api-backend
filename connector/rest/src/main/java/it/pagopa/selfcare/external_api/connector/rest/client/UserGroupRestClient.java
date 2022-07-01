package it.pagopa.selfcare.external_api.connector.rest.client;

import it.pagopa.selfcare.external_api.connector.rest.model.UserGroupResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "${rest-client.user-groups.serviceCode}", url = "${rest-client.user-groups.base-url}")
public interface UserGroupRestClient {

    @GetMapping(value = "${rest-client.user-group.getUserGroups.path}")
    @ResponseBody
    List<UserGroupResponse> getUserGroups(@RequestParam(value = "institutionId", required = false) String institutionId,
                                          @RequestParam(value = "productId", required = false) String productId,
                                          @RequestParam(value = "userId", required = false) UUID memberId,
                                          Pageable pageable);
}
