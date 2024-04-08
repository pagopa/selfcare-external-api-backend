package it.pagopa.selfcare.external_api.connector.rest;

import it.pagopa.selfcare.external_api.api.UserMsConnector;
import it.pagopa.selfcare.external_api.connector.rest.mapper.UserMapper;
import it.pagopa.selfcare.external_api.model.user.User;
import it.pagopa.selfcare.external_api.model.user.UserInstitution;
import it.pagopa.selfcare.user.generated.openapi.v1.api.UserControllerApi;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.SearchUserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class UserMsConnectorImpl implements UserMsConnector {

    private final UserControllerApi userControllerApi;

    private final UserMapper userMapper;

    public UserMsConnectorImpl(UserControllerApi userControllerApi,
                               UserMapper userMapper) {
        this.userControllerApi = userControllerApi;
        this.userMapper = userMapper;
    }

    @Override
    public List<UserInstitution> getUsersInstitutions(String userId) {
        return Objects.requireNonNull(userControllerApi._usersGet(
                null, null, null, null,
                null, null, null, userId).getBody())
                .stream().map(userMapper::toUserInstitutionsFromUserInstitutionResponse).toList();
    }

    @Override
    public User searchUserByExternalId(String fiscalCode) {
        SearchUserDto searchUserDto = new SearchUserDto(fiscalCode);
        return userMapper.toUserFromUserDetailResponse(userControllerApi._usersSearchPost(null, searchUserDto).getBody());
    }
}