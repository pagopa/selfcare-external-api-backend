package it.pagopa.selfcare.external_api.connector.rest;

import it.pagopa.selfcare.external_api.connector.rest.client.MsOnboardingTokenControllerApi;
import it.pagopa.selfcare.external_api.connector.rest.mapper.TokenMapper;
import it.pagopa.selfcare.external_api.connector.rest.mapper.TokenMapperImpl;
import it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.TokenResponse;
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
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
public class OnboardingMsConnectorImplTest {

    @InjectMocks
    private OnboardingMsConnectorImpl onboardingMsConnector;

    @Mock
    MsOnboardingTokenControllerApi tokenControllerApi;

    @Spy
    TokenMapper tokenMapper = new TokenMapperImpl();

    @Test
    void getToken(){
        //given
        final String onboardingId = "onboardingId";
        when(tokenControllerApi._v1TokensGet(onboardingId))
                .thenReturn(ResponseEntity.of(Optional.of(List.of(new TokenResponse()))));
        //when
        onboardingMsConnector.getToken(onboardingId);
        //then
        verify(tokenControllerApi, times(1))
                ._v1TokensGet(onboardingId);
        verifyNoMoreInteractions(tokenControllerApi);

    }
}
