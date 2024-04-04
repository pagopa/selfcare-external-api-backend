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
import it.pagopa.selfcare.external_api.web.model.user.UserInfoResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping(value = "/v2/users", produces = APPLICATION_JSON_VALUE)
@Api(tags = "users")
public class UserV2Controller {

    private final UserService userService;
    private final UserInfoResourceMapper userInfoResourceMapper;

    @Autowired
    public UserV2Controller(UserService userService, UserInfoResourceMapper userInfoResourceMapper) {
        this.userService = userService;
        this.userInfoResourceMapper = userInfoResourceMapper;
    }

    @Tags({@Tag(name = "support"), @Tag(name = "external-v2"), @Tag(name = "users")})
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.external_api.user.api.getUserInfo}")
    public UserInfoResource getUserInfo(@ApiParam("${swagger.external_api.user.model.searchUser}")
                                        @RequestBody @Valid SearchUserDto searchUserDto) {
        log.trace("getUserInfo start");
        UserInfoWrapper userInfo = userService.getUserInfoV2(searchUserDto.getFiscalCode(), searchUserDto.getStatuses());
        UserInfoResource userInfoResource = userInfoResourceMapper.toResource(userInfo);
        log.debug("getUserInfo result = {}", userInfoResource);
        log.trace("getUserInfo end");
        return userInfoResource;
    }
}
