package it.pagopa.selfcare.external_api.connector.rest;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.external_api.connector.rest.client.MsUserApiRestClient;
import it.pagopa.selfcare.external_api.connector.rest.mapper.UserMapper;
import it.pagopa.selfcare.external_api.connector.rest.mapper.UserMapperImpl;
import it.pagopa.selfcare.external_api.connector.rest.mapper.UserResourceMapper;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.user.UserToOnboard;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserMsConnectorImplTest {
    @InjectMocks
    private UserMsConnectorImpl userMsConnector;

    @Mock
    private MsUserApiRestClient msUserApiRestClient;

    @Mock
    private UserResourceMapper userResourceMapper;

    @Mock
    private UserMapper userMapper;

    @Spy
    UserMapper mapper = new UserMapperImpl();

    @Test
    void getUserInstitutions(){
        final List<PartyRole> commonsPartyRoles = List.of(PartyRole.OPERATOR);
        when(msUserApiRestClient._usersGet(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(new ResponseEntity<>(List.of(new UserInstitutionResponse()), HttpStatus.OK));
        userMsConnector.getUsersInstitutions(null, null, null, null, null, null, commonsPartyRoles, null);

        verify(msUserApiRestClient, times(1))._usersGet(null, null, null, null, List.of(it.pagopa.selfcare.user.generated.openapi.v1.dto.PartyRole.OPERATOR), null, null, null);
    }

    @Test
    void getUserInstitutions_nullRoles(){
        when(msUserApiRestClient._usersGet(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(new ResponseEntity<>(List.of(new UserInstitutionResponse()), HttpStatus.OK));

        userMsConnector.getUsersInstitutions(null, null, null, null, null, null, null, null);

        verify(msUserApiRestClient, times(1))._usersGet(null, null, null, null, Collections.emptyList(), null, null, null);
    }

    @Test
    void createUser_happyPath() {
        Institution institution = new Institution();
        institution.setId("institutionId");
        institution.setDescription("description");
        institution.setParentDescription("parentDescription");

        UserToOnboard userToOnboard = new UserToOnboard();
        userToOnboard.setId("userId");

        when(userResourceMapper.toUser(any(UserToOnboard.class))).thenReturn(new User());
        when(msUserApiRestClient._usersPost(any(CreateUserDto.class))).thenReturn(new ResponseEntity<>("userId", HttpStatus.OK));

        String userId = userMsConnector.createUser(institution, "productId", "MANAGER", List.of("productRole"), userToOnboard);

        verify(msUserApiRestClient, times(1))._usersPost(any(CreateUserDto.class));
        verify(userResourceMapper, times(1)).toUser(any(UserToOnboard.class));
        assert userId.equals("userId");
    }

    @Test
    void createUser_nullUserToOnboard() {
        Institution institution = new Institution();
        institution.setId("institutionId");
        institution.setDescription("description");
        institution.setParentDescription("parentDescription");

        when(msUserApiRestClient._usersPost(any(CreateUserDto.class))).thenReturn(new ResponseEntity<>("userId", HttpStatus.OK));

        String userId = userMsConnector.createUser(institution, "productId", "MANAGER", List.of("productRole"), null);

        verify(msUserApiRestClient, times(1))._usersPost(any(CreateUserDto.class));
        assert userId.equals("userId");
    }

    @Test
    void addUserRole_happyPath() {
        Institution institution = new Institution();
        institution.setId("institutionId");
        institution.setDescription("description");
        institution.setParentDescription("parentDescription");

        when(msUserApiRestClient._usersUserIdPost(anyString(), any(AddUserRoleDto.class))).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        String returnedUserId = userMsConnector.addUserRole("userId", institution, "productId", "MANAGER", List.of("productRole"));

        verify(msUserApiRestClient, times(1))._usersUserIdPost(anyString(), any(AddUserRoleDto.class));
        assert returnedUserId.equals("userId");
    }

    @Test
    void addUserRole_nullProductRoles() {
        Institution institution = new Institution();
        institution.setId("institutionId");
        institution.setDescription("description");
        institution.setParentDescription("parentDescription");

        when(msUserApiRestClient._usersUserIdPost(anyString(), any(AddUserRoleDto.class))).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        String returnedUserId = userMsConnector.addUserRole("userId", institution, "productId", "MANAGER", null);

        verify(msUserApiRestClient, times(1))._usersUserIdPost(anyString(), any(AddUserRoleDto.class));
        assert returnedUserId.equals("userId");
    }
}