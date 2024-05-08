package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.external_api.api.FileStorageConnector;
import it.pagopa.selfcare.external_api.api.MsCoreConnector;
import it.pagopa.selfcare.external_api.api.OnboardingMsConnector;
import it.pagopa.selfcare.external_api.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.external_api.model.documents.ResourceResponse;
import it.pagopa.selfcare.external_api.model.onboarding.InstitutionOnboarding;
import it.pagopa.selfcare.external_api.model.token.Token;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ContractServiceImplTest {

    @InjectMocks
    private ContractServiceImpl contractService;

    @Mock
    private MsCoreConnector msCoreConnectorMock;

    @Mock
    private FileStorageConnector storageConnectorMock;

    @Mock
    private OnboardingMsConnector onboardingMsConnectorMock;


    @Test
    void getContractV2(){
        //given
        final String institutionId = "institutionId";
        final String productId = "productId";
        final InstitutionOnboarding institutionOnboarding = new InstitutionOnboarding();
        institutionOnboarding.setTokenId("tokenId");
        final Token token = new Token();
        token.setContractSigned("contractSigned");

        ResourceResponse response = mockInstance(new ResourceResponse());
        when(msCoreConnectorMock.getInstitutionOnboardings(institutionId, productId))
                .thenReturn(institutionOnboarding);

        when(onboardingMsConnectorMock.getToken(institutionOnboarding.getTokenId()))
                .thenReturn(List.of(token));

        when(storageConnectorMock.getFile(any())).thenReturn(response);

        //when
        ResourceResponse result = contractService.getContractV2(institutionId, productId);
        //then
        assertEquals(response, result);
        verify(msCoreConnectorMock, times(1)).getInstitutionOnboardings(institutionId, productId);
        verify(storageConnectorMock, times(1)).getFile(token.getContractSigned());
        verifyNoMoreInteractions(msCoreConnectorMock, storageConnectorMock);
    }


    @Test
    void getContractV2_tokenSignedIsEmpty(){
        //given
        final String institutionId = "institutionId";
        final String productId = "productId";
        final InstitutionOnboarding institutionOnboarding = new InstitutionOnboarding();
        institutionOnboarding.setTokenId("tokenId");
        final Token token = new Token();

        when(msCoreConnectorMock.getInstitutionOnboardings(institutionId, productId))
                .thenReturn(institutionOnboarding);

        when(onboardingMsConnectorMock.getToken(institutionOnboarding.getTokenId()))
                .thenReturn(List.of(token));

        //when
        assertThrows(ResourceNotFoundException.class, () -> contractService.getContractV2(institutionId, productId));
    }


    @Test
    void getContractV2_tokenNotFound(){
        //given
        final String institutionId = "institutionId";
        final String productId = "productId";
        final InstitutionOnboarding institutionOnboarding = new InstitutionOnboarding();
        institutionOnboarding.setTokenId("tokenId");

        when(msCoreConnectorMock.getInstitutionOnboardings(institutionId, productId))
                .thenReturn(institutionOnboarding);

        when(onboardingMsConnectorMock.getToken(institutionOnboarding.getTokenId()))
                .thenReturn(List.of());

        //when
        assertThrows(ResourceNotFoundException.class, () -> contractService.getContractV2(institutionId, productId));
    }

}