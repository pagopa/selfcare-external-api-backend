package it.pagopa.selfcare.external_api.connector.rest;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.external_api.api.UserMsConnector;
import it.pagopa.selfcare.external_api.connector.rest.mapper.UserMapper;
import it.pagopa.selfcare.external_api.model.user.User;
import it.pagopa.selfcare.external_api.model.user.UserInstitution;
import it.pagopa.selfcare.user.generated.openapi.v1.api.UserControllerApi;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.SearchUserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
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
    public List<UserInstitution> getUsersInstitutions(String userId, String institutionId, Integer page, Integer size, List<String> productRoles, List<String> products, List<PartyRole> roles, List<String> states) {

        return Objects.requireNonNull(userControllerApi._usersGet(
                        institutionId, page, productRoles, products, toDtoPartyRole(roles)
                        , size, states, userId).getBody())
                .stream().map(userMapper::toUserInstitutionsFromUserInstitutionResponse).toList();
    }

    @Override
    public User searchUserByExternalId(String fiscalCode) {
        SearchUserDto searchUserDto = new SearchUserDto(fiscalCode);
        return userMapper.toUserFromUserDetailResponse(userControllerApi._usersSearchPost(null, searchUserDto).getBody());
    }

    private List<it.pagopa.selfcare.user.generated.openapi.v1.dto.PartyRole> toDtoPartyRole(List<PartyRole> roles) {
        List<it.pagopa.selfcare.user.generated.openapi.v1.dto.PartyRole> partyRoles = new ArrayList<>();
        if (roles != null) {
            roles.forEach(partyRole -> {
                it.pagopa.selfcare.user.generated.openapi.v1.dto.PartyRole role = it.pagopa.selfcare.user.generated.openapi.v1.dto.PartyRole.valueOf(partyRole.name());
                partyRoles.add(role);
            });
        } else {
            return Collections.emptyList();
        }
        return partyRoles;
    }
}
