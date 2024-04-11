package it.pagopa.selfcare.external_api.connector.rest;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.external_api.connector.rest.client.UserApiRestClient;
import it.pagopa.selfcare.external_api.connector.rest.mapper.UserMapper;
import it.pagopa.selfcare.external_api.connector.rest.mapper.UserMapperImpl;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.UserInstitutionResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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

        verify(userApiRestClient, times(1))._usersGet(null, null, null, null, null, null, null, null);
    }

}