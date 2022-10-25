package it.pagopa.selfcare.external_api.connector.rest;

import it.pagopa.selfcare.external_api.connector.rest.client.UserRegistryRestClient;
import it.pagopa.selfcare.external_api.model.user.User;
import it.pagopa.selfcare.external_api.model.user.WorkContact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.external_api.model.user.Certification.NONE;
import static it.pagopa.selfcare.external_api.model.user.Certification.SPID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = {
                UserRegistryConnectorImpl.class
        }
)
class UserRegistryConnectorImplTest {

    @Autowired
    private UserRegistryConnectorImpl userConnector;

    @MockBean
    private UserRegistryRestClient restClientMock;


    @Test
    void getUserByInternalId_nullInfo() {
        //given
        UUID userId = UUID.randomUUID();
        final EnumSet<User.Fields> fieldList = EnumSet.allOf(User.Fields.class);
        User userMock = new User();
        when(restClientMock.getUserByInternalId(any(), any()))
                .thenReturn(userMock);
        //when
        User user = userConnector.getUserByInternalId(userId.toString(), fieldList);
        ///then
        assertNull(user.getName());
        assertNull(user.getFamilyName());
        assertNull(user.getEmail());
        assertNull(user.getId());
        assertNull(user.getWorkContacts());
        assertNull(user.getFiscalCode());
        verify(restClientMock, times(1))
                .getUserByInternalId(userId, EnumSet.allOf(User.Fields.class));
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUserByInternalId_nullInfo_nullUserResponse() {
        //given
        UUID userId = UUID.randomUUID();
        final EnumSet<User.Fields> fieldList = EnumSet.allOf(User.Fields.class);
        User userMock = null;
        when(restClientMock.getUserByInternalId(any(), any()))
                .thenReturn(userMock);
        //when
        User user = userConnector.getUserByInternalId(userId.toString(), fieldList);
        ///then
        assertNull(user);

        verify(restClientMock, times(1))
                .getUserByInternalId(userId, EnumSet.allOf(User.Fields.class));
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUserByInternalId_certificationNone() {
        //given
        UUID userId = UUID.randomUUID();
        final EnumSet<User.Fields> fieldList = EnumSet.allOf(User.Fields.class);
        User userMock = mockInstance(new User());
        userMock.setId(userId.toString());
        Map<String, WorkContact> workContacts = new HashMap<>();
        WorkContact workContact = mockInstance(new WorkContact());
        workContact.getEmail().setCertification(NONE);
        userMock.setWorkContacts(workContacts);
        workContacts.put("institutionId", workContact);
        when(restClientMock.getUserByInternalId(any(), any()))
                .thenReturn(userMock);
        //when
        User user = userConnector.getUserByInternalId(userId.toString(), fieldList);
        ///then
        assertEquals(userId.toString(), user.getId());
        assertEquals(NONE, user.getName().getCertification());
        assertEquals(NONE, user.getEmail().getCertification());
        assertEquals(NONE, user.getFamilyName().getCertification());
        user.getWorkContacts().forEach((key1, value) -> assertEquals(NONE, value.getEmail().getCertification()));
        assertNotNull(user.getFiscalCode());

        verify(restClientMock, times(1))
                .getUserByInternalId(userId, EnumSet.allOf(User.Fields.class));
        verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getUserByInternalId_nullExternalId() {
        //given
        String userId = null;
        final EnumSet<User.Fields> fieldList = EnumSet.allOf(User.Fields.class);
        //when
        Executable executable = () -> userConnector.getUserByInternalId(userId, fieldList);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A userId is required", e.getMessage());
        verifyNoInteractions(restClientMock);
    }

    @Test
    void getUserByInternalId_certificationNotNone() {
        //given
        UUID userId = UUID.randomUUID();
        final EnumSet<User.Fields> fieldList = EnumSet.allOf(User.Fields.class);
        User userMock = mockInstance(new User());
        userMock.setId(userId.toString());
        userMock.getEmail().setCertification(SPID);
        userMock.getFamilyName().setCertification(SPID);
        userMock.getName().setCertification(SPID);
        Map<String, WorkContact> workContacts = new HashMap<>();
        WorkContact workContact = mockInstance(new WorkContact());
        workContact.getEmail().setCertification(SPID);
        userMock.setWorkContacts(workContacts);
        workContacts.put("institutionId", workContact);
        when(restClientMock.getUserByInternalId(any(), any()))
                .thenReturn(userMock);
        //when
        User user = userConnector.getUserByInternalId(userId.toString(), fieldList);
        ///then
        assertEquals(userId.toString(), user.getId());
        assertEquals(SPID, user.getName().getCertification());
        assertEquals(SPID, user.getEmail().getCertification());
        assertEquals(SPID, user.getFamilyName().getCertification());
        user.getWorkContacts().forEach((key1, value) -> assertEquals(SPID, value.getEmail().getCertification()));
        assertNotNull(user.getFiscalCode());

        verify(restClientMock, times(1))
                .getUserByInternalId(userId, EnumSet.allOf(User.Fields.class));
        verifyNoMoreInteractions(restClientMock);
    }

}
