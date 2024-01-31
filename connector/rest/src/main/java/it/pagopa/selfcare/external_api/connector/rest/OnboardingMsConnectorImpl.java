package it.pagopa.selfcare.external_api.connector.rest;

import it.pagopa.selfcare.external_api.api.OnboardingMsConnector;
import it.pagopa.selfcare.external_api.connector.rest.client.MsOnboardingTokenControllerApi;
import it.pagopa.selfcare.external_api.connector.rest.mapper.TokenMapper;
import it.pagopa.selfcare.external_api.model.token.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class OnboardingMsConnectorImpl implements OnboardingMsConnector {

    private final MsOnboardingTokenControllerApi tokenControllerApi;
    private final TokenMapper tokenMapper;

    public OnboardingMsConnectorImpl(MsOnboardingTokenControllerApi tokenControllerApi, TokenMapper tokenMapper) {
        this.tokenControllerApi = tokenControllerApi;
        this.tokenMapper = tokenMapper;
    }

    @Override
    public List<Token> getToken(String onboardingId) {
        return Objects.requireNonNull(tokenControllerApi._v1TokensGet(onboardingId).getBody()).stream()
                .map(tokenMapper::toEntity)
                .toList();
    }
}
