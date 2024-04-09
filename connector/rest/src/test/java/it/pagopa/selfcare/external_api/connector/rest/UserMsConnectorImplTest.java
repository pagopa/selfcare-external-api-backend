package it.pagopa.selfcare.external_api.connector.rest;

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

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserMsConnectorImplTest {

    @InjectMocks
    private UserMsConnectorImpl userMsConnector;

    @Mock
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
