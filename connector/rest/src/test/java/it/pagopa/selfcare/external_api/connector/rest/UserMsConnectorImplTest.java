package it.pagopa.selfcare.external_api.connector.rest;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.external_api.connector.rest.client.UserApiRestClient;
import it.pagopa.selfcare.external_api.connector.rest.mapper.UserMapper;
import it.pagopa.selfcare.external_api.connector.rest.mapper.UserMapperImpl;
import it.pagopa.selfcare.external_api.connector.rest.mapper.UserMapper;
import it.pagopa.selfcare.external_api.connector.rest.mapper.UserMapperImpl;
import it.pagopa.selfcare.user.generated.openapi.v1.api.UserControllerApi;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.SearchUserDto;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.UserDetailResponse;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.UserInstitutionResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

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
    private UserApiRestClient userApiRestClient;

    @Spy
    UserMapper mapper = new UserMapperImpl();

    @Test
    void getUserInstitutions(){
        final List<PartyRole> commonsPartyRoles = List.of(PartyRole.OPERATOR);
        when(userApiRestClient._usersGet(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(new ResponseEntity<>(List.of(new UserInstitutionResponse()), HttpStatus.OK));
        userMsConnector.getUsersInstitutions(null, null, null, null, null, null, commonsPartyRoles, null);

        verify(userApiRestClient, times(1))._usersGet(null, null, null, null, List.of(it.pagopa.selfcare.user.generated.openapi.v1.dto.PartyRole.OPERATOR), null, null, null);
    }

    @Test
    void getUserInstitutions_nullRoles(){
        when(userApiRestClient._usersGet(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(new ResponseEntity<>(List.of(new UserInstitutionResponse()), HttpStatus.OK));

        userMsConnector.getUsersInstitutions(null, null, null, null, null, null, null, null);

        verify(userApiRestClient, times(1))._usersGet(null, null, null, null, Collections.emptyList(), null, null, null);
    }

}
    private UserControllerApi userControllerApi;

    @Spy
    private UserMapper userMapper = new UserMapperImpl();

    @Test
    void getUsersInstitutions() {
        final String userId = "userId";
        when(userControllerApi._usersGet(null, null, null, null, null, null, null, userId))
                .thenReturn(ResponseEntity.of(Optional.of(List.of(new UserInstitutionResponse()))));
        userMsConnector.getUsersInstitutions(userId, null);
        verify(userControllerApi, times(1))
                ._usersGet(null, null, null, null, null, null, null, userId);
        verifyNoMoreInteractions(userControllerApi);
    }

    @Test
    void getUsersInstitutionsWithProducts() {
        final String userId = "userId";
        final String productId = "prod-1";
        when(userControllerApi._usersGet(null, null, null, List.of(productId), null, null, null, userId))
                .thenReturn(ResponseEntity.of(Optional.of(List.of(new UserInstitutionResponse()))));
        userMsConnector.getUsersInstitutions(userId, List.of(productId));
        verify(userControllerApi, times(1))
                ._usersGet(null, null, null, List.of(productId), null, null, null, userId);
        verifyNoMoreInteractions(userControllerApi);
    }

    @Test
    void searchUserByExternalId() {
        final String fiscalCode = "fiscalCode";
        SearchUserDto searchUserDto = new SearchUserDto(fiscalCode);
        when(userControllerApi._usersSearchPost(null, searchUserDto))
                .thenReturn(ResponseEntity.of(Optional.of(new UserDetailResponse())));
        userMsConnector.searchUserByExternalId(fiscalCode);
        verify(userControllerApi, times(1))
                ._usersSearchPost(null, searchUserDto);
        verifyNoMoreInteractions(userControllerApi);
    }
}
