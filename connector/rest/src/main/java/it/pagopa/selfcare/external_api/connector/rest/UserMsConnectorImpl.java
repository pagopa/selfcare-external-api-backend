package it.pagopa.selfcare.external_api.connector.rest;

import it.pagopa.selfcare.external_api.api.UserMsConnector;
import it.pagopa.selfcare.external_api.connector.rest.client.MsUserApiRestClient;
import it.pagopa.selfcare.external_api.connector.rest.mapper.UserMapper;
import it.pagopa.selfcare.external_api.connector.rest.mapper.UserResourceMapper;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.user.User;
import it.pagopa.selfcare.external_api.model.user.UserInstitution;
import it.pagopa.selfcare.external_api.model.user.UserToOnboard;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


@Slf4j
@Component
public class UserMsConnectorImpl implements UserMsConnector {

    private final MsUserApiRestClient msUserApiRestClient;
    private final UserResourceMapper userResourceMapper;

    private final UserMapper userMapper;

    public UserMsConnectorImpl(MsUserApiRestClient msUserApiRestClient, UserResourceMapper userResourceMapper,  UserMapper userMapper) {
        this.msUserApiRestClient = msUserApiRestClient;
        this.userResourceMapper = userResourceMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<UserInstitution> getUsersInstitutions(String userId, String institutionId, Integer page, Integer size, List<String> productRoles, List<String> products, List<it.pagopa.selfcare.commons.base.security.PartyRole> roles, List<String> states) {

        return Objects.requireNonNull(msUserApiRestClient._usersGet(
                        institutionId, page, productRoles, products, toDtoPartyRole(roles)
                        , size, states, userId).getBody())
                .stream().map(userMapper::toUserInstitutionsFromUserInstitutionResponse).toList();
    }

    @Override
    public User searchUserByExternalId(String fiscalCode) {
        SearchUserDto searchUserDto = new SearchUserDto(fiscalCode);
        return userMapper.toUserFromUserDetailResponse(msUserApiRestClient._usersSearchPost(null, searchUserDto).getBody());
    }

    private List<it.pagopa.selfcare.user.generated.openapi.v1.dto.PartyRole> toDtoPartyRole(List<it.pagopa.selfcare.commons.base.security.PartyRole> roles) {
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

    @Override
    public String createUser(Institution institution, String productId, String role, List<String> productRoles, UserToOnboard user) {

        Product1 product = Product1.builder()
                .productId(productId)
                .role(PartyRole.valueOf(role))
                .productRoles(productRoles)
                .build();

        CreateUserDto createUserDto = CreateUserDto.builder()
                .institutionId(institution.getId())
                .user(userResourceMapper.toUser(user))
                .product(product)
                .institutionDescription(institution.getDescription())
                .institutionRootName(institution.getParentDescription())
                .build();

        String userId = msUserApiRestClient._usersPost(createUserDto).getBody();
        log.info("User created with id: {}", userId);
        return userId;
    }

    @Override
    public String addUserRole(String userId, Institution institution, String productId, String role, List<String> productRoles) {
        it.pagopa.selfcare.user.generated.openapi.v1.dto.Product product = it.pagopa.selfcare.user.generated.openapi.v1.dto.Product.builder()
                .productId(productId)
                .role(PartyRole.valueOf(role))
                .productRoles(productRoles)
                .build();

        AddUserRoleDto addUserRoleDto = AddUserRoleDto.builder()
                .product(product)
                .institutionId(institution.getId())
                .institutionDescription(institution.getDescription())
                .institutionRootName(institution.getParentDescription())
                .build();

        msUserApiRestClient._usersUserIdPost(userId, addUserRoleDto);

        return userId;
    }
}
