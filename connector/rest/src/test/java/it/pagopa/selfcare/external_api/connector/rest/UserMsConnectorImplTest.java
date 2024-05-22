package it.pagopa.selfcare.external_api.connector.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.external_api.connector.rest.client.MsUserApiRestClient;
import it.pagopa.selfcare.external_api.connector.rest.config.BaseConnectorTest;
import it.pagopa.selfcare.external_api.connector.rest.mapper.UserMapperImpl;
import it.pagopa.selfcare.external_api.connector.rest.mapper.UserResourceMapperImpl;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.user.UserInstitution;
import it.pagopa.selfcare.external_api.model.user.UserToOnboard;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserMsConnectorImplTest extends BaseConnectorTest {

    @InjectMocks
    private UserMsConnectorImpl userMsConnector;

    @Mock
    private MsUserApiRestClient msUserApiRestClient;

    @Spy
    private UserResourceMapperImpl userResourceMapper;

    @Spy
    private UserMapperImpl userMapper;


    @Test
    void getUserInstitutions_EmptyList() {
        String institutionId = "institutionId";
        String userId = "userId";
        PartyRole partyRole = PartyRole.MANAGER;

        when(msUserApiRestClient._usersGet(eq(institutionId), any(), any(), any(), any(), any(), any(), eq(userId))).thenReturn(ResponseEntity.of(Optional.of(Collections.emptyList())));

        List<UserInstitution> userInstitutions = userMsConnector.getUsersInstitutions(userId, institutionId, null, null, null, null, List.of(partyRole), null);

        assertEquals(0, userInstitutions.size());
        verify(msUserApiRestClient, times(1))._usersGet(eq(institutionId), any(), any(), any(), any(), any(), any(), eq(userId));
    }

    @Test
    void getUserInstitution() throws IOException {
        String institutionId = "institutionId";
        String userId = "userId";

        ClassPathResource resource = new ClassPathResource("stubs/UserInstitutionResponse.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        List<UserInstitutionResponse> userInstitutions = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        ClassPathResource classPathResource = new ClassPathResource("stubs/UserInstitution.json");
        byte[] bytes = Files.readAllBytes(classPathResource.getFile().toPath());
        List<UserInstitution> expectation = objectMapper.readValue(bytes, new TypeReference<>() {
        });

        when(msUserApiRestClient._usersGet(eq(institutionId), any(), any(), any(), any(), any(), any(), eq(userId))).thenReturn(ResponseEntity.of(Optional.of(userInstitutions)));

        List<UserInstitution> actualUserInstitutions = userMsConnector.getUsersInstitutions(userId,institutionId, null, null, null, null, null, null);

        assertEquals(expectation, actualUserInstitutions);
        verify(msUserApiRestClient, times(1))._usersGet(eq(institutionId), any(), any(), any(), any(), any(), any(), eq(userId));
    }

    @Test
    public void searchUserByExternalId_returnsUser() {

        String fiscalCode = "fiscalCode";
        SearchUserDto searchUserDto = new SearchUserDto(fiscalCode);
        UserDetailResponse userDetailResponse = new UserDetailResponse();
        it.pagopa.selfcare.external_api.model.user.User expectedUser = new it.pagopa.selfcare.external_api.model.user.User();

        when(msUserApiRestClient._usersSearchPost(null, searchUserDto)).thenReturn(ResponseEntity.of(Optional.of(userDetailResponse)));
        when(userMapper.toUserFromUserDetailResponse(userDetailResponse)).thenReturn(expectedUser);

        it.pagopa.selfcare.external_api.model.user.User actualUser = userMsConnector.searchUserByExternalId(fiscalCode);

        assertEquals(expectedUser, actualUser);
        verify(msUserApiRestClient, times(1))._usersSearchPost(null, searchUserDto);

    }

    @Test
    public void searchUserByExternalId_returnsNull_whenUserDoesNotExist() {

        String fiscalCode = "fiscalCode";
        SearchUserDto searchUserDto = new SearchUserDto(fiscalCode);

        when(msUserApiRestClient._usersSearchPost(null, searchUserDto)).thenReturn(ResponseEntity.of(Optional.empty()));

        it.pagopa.selfcare.external_api.model.user.User actualUser = userMsConnector.searchUserByExternalId(fiscalCode);

        assertNull(actualUser);
        verify(msUserApiRestClient, times(1))._usersSearchPost(null, searchUserDto);
    }

    @Test
    void createUser() {
        Institution institution = new Institution();
        institution.setId("institutionId");
        institution.setDescription("description");
        institution.setParentDescription("parentDescription");

        UserToOnboard userToOnboard = new UserToOnboard();
        userToOnboard.setId("userId");

        when(userResourceMapper.toUser(any(UserToOnboard.class))).thenReturn(new User());
        when(msUserApiRestClient._usersPost(any(CreateUserDto.class))).thenReturn(new ResponseEntity<>("userId", HttpStatus.OK));

        String userId = userMsConnector.createUser(institution, "productId", "MANAGER", List.of("productRole"), userToOnboard, false);

        verify(msUserApiRestClient, times(1))._usersPost(any(CreateUserDto.class));
        verify(userResourceMapper, times(1)).toUser(any(UserToOnboard.class));
        assert userId.equals("userId");
    }

    @Test
    void addUserRole() {
        Institution institution = new Institution();
        institution.setId("institutionId");
        institution.setDescription("description");
        institution.setParentDescription("parentDescription");

        when(msUserApiRestClient._usersUserIdPost(anyString(), any(AddUserRoleDto.class))).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        String returnedUserId = userMsConnector.addUserRole("userId", institution, "productId", "MANAGER", List.of("productRole"));

        verify(msUserApiRestClient, times(1))._usersUserIdPost(anyString(), any(AddUserRoleDto.class));
        assert returnedUserId.equals("userId");
    }
}
