package it.pagopa.selfcare.external_api.connector.rest;

import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import it.pagopa.selfcare.external_api.api.OnboardingMsConnector;
import it.pagopa.selfcare.external_api.connector.rest.client.MsOnboardingControllerApi;
import it.pagopa.selfcare.external_api.connector.rest.client.MsOnboardingTokenControllerApi;
import it.pagopa.selfcare.external_api.connector.rest.mapper.OnboardingMapper;
import it.pagopa.selfcare.external_api.connector.rest.mapper.TokenMapper;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingData;
import it.pagopa.selfcare.external_api.model.token.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class OnboardingMsConnectorImpl implements OnboardingMsConnector {

    private final MsOnboardingTokenControllerApi tokenControllerApi;
    private final MsOnboardingControllerApi onboardingControllerApi;
    private final TokenMapper tokenMapper;
    private final OnboardingMapper onboardingMapper;

    public OnboardingMsConnectorImpl(MsOnboardingTokenControllerApi tokenControllerApi,
                                     MsOnboardingControllerApi onboardingControllerApi,
                                     OnboardingMapper onboardingMapper,
                                     TokenMapper tokenMapper) {
        this.tokenControllerApi = tokenControllerApi;
        this.onboardingControllerApi = onboardingControllerApi;
        this.tokenMapper = tokenMapper;
        this.onboardingMapper = onboardingMapper;
    }

    @Override
    public List<Token> getToken(String onboardingId) {
        return Objects.requireNonNull(tokenControllerApi._v1TokensGet(onboardingId).getBody()).stream()
                .map(tokenMapper::toEntity)
                .toList();
    }

    @Override
    public void onboarding(OnboardingData onboardingData) {
        if (onboardingData.getInstitutionType() == InstitutionType.PA) {
            onboardingControllerApi._v1OnboardingPaCompletionPost(onboardingMapper.toOnboardingPaRequest(onboardingData));
        } else if (onboardingData.getInstitutionType() == InstitutionType.PSP) {
            onboardingControllerApi._v1OnboardingPspCompletionPost(onboardingMapper.toOnboardingPspRequest(onboardingData));
        } else {
            onboardingControllerApi._v1OnboardingCompletionPost(onboardingMapper.toOnboardingDefaultRequest(onboardingData));
        }
    }

}
