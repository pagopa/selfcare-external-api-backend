package it.pagopa.selfcare.external_api.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import it.pagopa.selfcare.external_api.core.UserService;
import it.pagopa.selfcare.external_api.model.user.UserInfoWrapper;
import it.pagopa.selfcare.external_api.web.model.mapper.UserInfoResourceMapper;
import it.pagopa.selfcare.external_api.web.model.user.SearchUserDto;
import it.pagopa.selfcare.external_api.web.model.user.UserDetailsResource;
import it.pagopa.selfcare.external_api.web.model.user.UserInfoResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping(value = "/users", produces = APPLICATION_JSON_VALUE)
@Api(tags = "users")
public class UserController {

    private final UserService userService;
    private final UserInfoResourceMapper userInfoResourceMapper;

    @Autowired
    public UserController(UserService userService, UserInfoResourceMapper userInfoResourceMapper) {
        this.userService = userService;
        this.userInfoResourceMapper = userInfoResourceMapper;
    }

    @Tags({@Tag(name = "support"), @Tag(name = "external-v2")})
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.external_api.user.api.getUserInfo}")
    public UserInfoResource getUserInfo(@ApiParam("${swagger.external_api.user.model.searchUser}")
                                        @RequestBody @Valid SearchUserDto searchUserDto) {
        log.trace("getUserInfo start");
        log.debug("getUserInfo searchUserDto = {}", searchUserDto);
        UserInfoWrapper userInfo = userService.getUserInfo(searchUserDto.getFiscalCode(), searchUserDto.getStatuses());
        UserInfoResource userInfoResource = userInfoResourceMapper.toResource(userInfo);
        log.debug("getUserInfo result = {}", userInfoResource);
        log.trace("getUserInfo end");
        return userInfoResource;
    }

    @GetMapping(value = "/{id}/onboarded-product")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.external_api.user.api.getUserProductInfo}")
    public UserDetailsResource getUserProductInfo(@ApiParam("${swagger.external_api.user.model.id}")
                                                  @PathVariable("id") String userId,
                                                  @ApiParam("${swagger.external-api.product.model.id}")
                                                  @RequestParam("productId") String productId,
                                                  @ApiParam("${swagger.external_api.institutions.model.id}")
                                                  @RequestParam("institutionId")
                                                  String institutionId) {
        log.trace("getUserProductInfo start");
        log.debug("getUserProductInfo userId = {}, productId = {}, institutionId = {}", userId, productId, institutionId);
        UserDetailsResource userDetailsResource = userInfoResourceMapper.toResource(userService.getUserOnboardedProductDetails(userId, institutionId, productId));
        log.debug("getUserProductInfo result = {}", userDetailsResource);
        log.trace("getUserProductInfo end");
        return userDetailsResource;
    }

}
