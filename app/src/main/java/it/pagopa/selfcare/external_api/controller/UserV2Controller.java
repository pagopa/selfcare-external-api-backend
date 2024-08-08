package it.pagopa.selfcare.external_api.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.pagopa.selfcare.external_api.mapper.UserInfoResourceMapper;
import it.pagopa.selfcare.external_api.model.user.*;
import it.pagopa.selfcare.external_api.service.UserService;
import it.pagopa.selfcare.onboarding.common.PartyRole;
import lombok.extern.slf4j.Slf4j;
import org.owasp.encoder.Encode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping(value = "/v2/users", produces = APPLICATION_JSON_VALUE)
@Api(tags = "User")
public class UserV2Controller {

    private final UserService userService;
    private final UserInfoResourceMapper userInfoResourceMapper;

    public UserV2Controller(UserService userService, UserInfoResourceMapper userInfoResourceMapper) {
        this.userService = userService;
        this.userInfoResourceMapper = userInfoResourceMapper;
    }
    @Tag(name = "support")
    @Tag(name = "support-pnpg")
    @Tag(name = "external-v2")
    @Tag(name = "User")
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.external_api.user.api.getUserInfo2}", nickname = "V2getUserInfoUsingGET")
    public UserInfoResource getUserInfo(@ApiParam("${swagger.external_api.user.model.searchUser}")
                                        @RequestBody @Valid SearchUserDto searchUserDto) {
        log.trace("getUserInfo start");
        UserInfoWrapper userInfo = userService.getUserInfoV2(searchUserDto.getFiscalCode(), searchUserDto.getStatuses());
        UserInfoResource userInfoResource = userInfoResourceMapper.toResource(userInfo);
        log.debug("getUserInfo result = {}", userInfoResource);
        log.trace("getUserInfo end");
        return userInfoResource;
    }

    @GetMapping(value = "/{id}/onboarded-product")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.external_api.user.api.getUserProductInfo}", nickname = "V2getUserProductInfoUsingGET")
    public UserDetailsResource getUserProductInfo(@ApiParam("${swagger.external_api.user.model.id}")
                                                  @PathVariable("id") String userId,
                                                  @ApiParam("${swagger.external-api.product.model.id}")
                                                  @RequestParam("productId") String productId,
                                                  @ApiParam("${swagger.external_api.institutions.model.id}")
                                                  @RequestParam("institutionId")
                                                  String institutionId) {
        log.trace("getUserProductInfo start");
        UserDetailsResource userDetailsResource = userInfoResourceMapper.toResource(userService.getUserOnboardedProductsDetailsV2(userId, institutionId, productId));
        log.debug("getUserProductInfo result = {}", userDetailsResource);
        log.trace("getUserProductInfo end");
        return userDetailsResource;
    }


    @Tag(name = "external-v2")
    @Tag(name = "User")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "The API retrieves paged users with optional filters in input as query params", nickname = "v2getUserInstitution")
    public List<UserInstitutionResource> getUserInstitution(@RequestParam(value = "institutionId", required = false) String institutionId,
                                                            @RequestParam(value = "userId", required = false) String userId,
                                                            @RequestParam(value = "roles", required = false) List<PartyRole> roles,
                                                            @RequestParam(value = "states", required = false) List<String> states,
                                                            @RequestParam(value = "products", required = false) List<String> products,
                                                            @RequestParam(value = "productRoles", required = false) List<String> productRoles,
                                                            @RequestParam(value = "page", defaultValue = "0") Integer page,
                                                            @RequestParam(value = "size", defaultValue = "100") Integer size) {

        log.trace("getUserInstitution start, institutionId: {}, userId: {}", Encode.forJava(institutionId), Encode.forJava(userId));
        List<UserInstitution> userInstitutions = userService.getUsersInstitutions(userId, institutionId, page, size, productRoles, products, roles, states);
        log.trace("getUserInstitution end");
        return userInstitutions.stream()
                .map(userInfoResourceMapper::toUserInstitutionResource)
                .toList();
    }
}
