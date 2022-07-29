package it.pagopa.selfcare.external_api.connector.rest;

import it.pagopa.selfcare.external_api.api.PartyConnector;
import it.pagopa.selfcare.external_api.connector.rest.client.PartyProcessRestClient;
import it.pagopa.selfcare.external_api.connector.rest.model.OnBoardingInfo;
import it.pagopa.selfcare.external_api.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.external_api.model.onboarding.RelationshipState.ACTIVE;

@Service
@Slf4j
public class PartyConnectorImpl implements PartyConnector {

    private final PartyProcessRestClient restClient;
    private static final BinaryOperator<InstitutionInfo> MERGE_FUNCTION =
            (inst1, inst2) -> inst1.getUserRole().compareTo(inst2.getUserRole()) < 0 ? inst1 : inst2;

    private static final Function<OnboardingResponseData, InstitutionInfo> ONBOARDING_DATA_TO_INSTITUTION_INFO_FUNCTION = onboardingData -> {
        InstitutionInfo institutionInfo = new InstitutionInfo();
        institutionInfo.setId(onboardingData.getId());
        institutionInfo.setExternalId(onboardingData.getExternalId());
        institutionInfo.setDescription(onboardingData.getDescription());
        institutionInfo.setStatus(onboardingData.getState().toString());
        institutionInfo.setTaxCode(onboardingData.getTaxCode());
        institutionInfo.setAddress(onboardingData.getAddress());
        institutionInfo.setDigitalAddress(onboardingData.getDigitalAddress());
        institutionInfo.setZipCode(onboardingData.getZipCode());
        institutionInfo.setBilling(onboardingData.getBilling());
        institutionInfo.setOrigin(onboardingData.getOrigin());
        institutionInfo.setOriginId(onboardingData.getOriginId());
        institutionInfo.setInstitutionType(onboardingData.getInstitutionType());
        institutionInfo.setUserRole(onboardingData.getRole());
        return institutionInfo;
    };

    @Autowired
    public PartyConnectorImpl(PartyProcessRestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Collection<InstitutionInfo> getOnBoardedInstitutions(String productId) {
        log.trace("getOnBoardedInstitutions start");
        log.debug("getOnboardedInstitutions productId = {}", productId);
        Assert.hasText(productId, "A productId is required");
        OnBoardingInfo onBoardingInfo = restClient.getOnBoardingInfo(null, EnumSet.of(ACTIVE));
        Collection<InstitutionInfo> result = parseOnBoardingInfo(onBoardingInfo, productId);
        log.debug("getOnBoardedInstitutions result = {}", result);
        log.trace("getOnBoardedInstitutions end");
        return result;
    }

    private Collection<InstitutionInfo> parseOnBoardingInfo(OnBoardingInfo onBoardingInfo, String productId) {
        log.trace("parseOnBoardingInfo start");
        log.debug("parseOnBoardingInfo onBoardingInfo = {}", onBoardingInfo);
        Collection<InstitutionInfo> institutions = Collections.emptyList();
        if (onBoardingInfo != null && onBoardingInfo.getInstitutions() != null) {
            institutions = onBoardingInfo.getInstitutions().stream()
                    .filter(onboardingResponseData -> onboardingResponseData.getProductInfo().getId().equals(productId))
                    .map(ONBOARDING_DATA_TO_INSTITUTION_INFO_FUNCTION)
                    .collect(Collectors.collectingAndThen(
                            Collectors.toMap(InstitutionInfo::getId, Function.identity(), MERGE_FUNCTION),
                            Map::values
                    ));
        }
        log.debug("parseOnBoardingInfo result = {}", institutions);
        log.trace("parseOnBoardingInfo end");
        return institutions;
    }
}
