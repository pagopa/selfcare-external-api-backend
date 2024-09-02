package it.pagopa.selfcare.external_api.connector.rest;

import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import it.pagopa.selfcare.external_api.api.OnboardingMsConnector;
import it.pagopa.selfcare.external_api.connector.rest.client.MsCoreInstitutionApiClient;
import it.pagopa.selfcare.external_api.connector.rest.client.MsOnboardingControllerApi;
import it.pagopa.selfcare.external_api.connector.rest.client.MsOnboardingTokenControllerApi;
import it.pagopa.selfcare.external_api.connector.rest.mapper.OnboardingMapper;
import it.pagopa.selfcare.external_api.connector.rest.mapper.TokenMapper;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingData;
import it.pagopa.selfcare.external_api.model.token.Token;
import it.pagopa.selfcare.external_api.model.token.TokenOnboardedUsers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class OnboardingMsConnectorImpl implements OnboardingMsConnector {

    private final MsOnboardingTokenControllerApi tokenControllerApi;
    private final MsOnboardingControllerApi onboardingControllerApi;
    private final MsCoreInstitutionApiClient institutionApiClient;
    private final TokenMapper tokenMapper;
    private final OnboardingMapper onboardingMapper;


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

    @Override
    public void onboardingImportPA(OnboardingData onboardingData) {
        onboardingControllerApi._v1OnboardingPaImportPost(onboardingMapper.toOnboardingImportRequest(onboardingData));
    }

    @Override
    public List<TokenOnboardedUsers> getOnboardings(String productId, int page, int size, String status) {
        return Objects.requireNonNull(
                        onboardingControllerApi._v1OnboardingGet(null, null, null, page, productId, size, status, null, null, null).getBody())
                .getItems().stream()
                .map(tokenMapper::toEntity)
                .toList();
    }

    @Override
    public List<Institution> getInstitutionsByTaxCodeAndSubunitCode(String institutionTaxCode, String institutionSubunitCode) {
        return Objects.requireNonNull(institutionApiClient._getInstitutionsUsingGET(institutionTaxCode, institutionSubunitCode, null, null)
                        .getBody())
                        .getInstitutions()
                        .stream()
                        .map(onboardingMapper::toInstitution)
                .toList();
    }

}
