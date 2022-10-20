package it.pagopa.selfcare.external_api.connector.rest.client;

import it.pagopa.selfcare.external_api.model.user.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.EnumSet;
import java.util.UUID;

@FeignClient(name = "${rest-client.user-registry.serviceCode}", url = "${rest-client.user-registry.base-url}")
public interface UserRegistryRestClient {

    @GetMapping(value = "${rest-client.user-registry.getUserByInternalId.path}")
    @ResponseBody
    User getUserByInternalId(@PathVariable("id") UUID id,
                             @RequestParam(value = "fl") EnumSet<User.Fields> fieldList);

}
