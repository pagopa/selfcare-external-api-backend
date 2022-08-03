package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.external_api.api.PartyConnector;
import it.pagopa.selfcare.external_api.model.institutions.InstitutionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstitutionServiceImplTest {
    @InjectMocks
    private InstitutionServiceImpl institutionService;

    @Mock
    private PartyConnector partyConnectorMock;

    @Test
    void getInstitutions() {
        //given
        String productId = "productIds";
        InstitutionInfo expectedInstitutionInfo = new InstitutionInfo();
        when(partyConnectorMock.getOnBoardedInstitutions(anyString()))
                .thenReturn(List.of(expectedInstitutionInfo));
        // when
        Collection<InstitutionInfo> institutions = institutionService.getInstitutions(productId);
        // then
        assertNotNull(institutions);
        assertEquals(1, institutions.size());
        assertSame(expectedInstitutionInfo, institutions.iterator().next());
        verify(partyConnectorMock, times(1))
                .getOnBoardedInstitutions(productId);
        verifyNoMoreInteractions(partyConnectorMock);
    }

    @Test
    void getInstitutions_emptyResult() {
        //given
        //when
        Collection<InstitutionInfo> institutions = institutionService.getInstitutions(null);
        // then
        assertNotNull(institutions);
        assertTrue(institutions.isEmpty());
        verify(partyConnectorMock, times(1))
                .getOnBoardedInstitutions(isNull());
        verifyNoMoreInteractions(partyConnectorMock);
    }
}