package it.pagopa.selfcare.external_api.connector.rest;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.external_api.api.PartyConnector;
import it.pagopa.selfcare.external_api.connector.rest.client.PartyProcessRestClient;
import it.pagopa.selfcare.external_api.connector.rest.model.institution.OnBoardingInfo;
import it.pagopa.selfcare.external_api.connector.rest.model.institution.RelationshipInfo;
import it.pagopa.selfcare.external_api.connector.rest.model.institution.RelationshipsResponse;
import it.pagopa.selfcare.external_api.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingResponseData;
import it.pagopa.selfcare.external_api.model.product.PartyProduct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.external_api.model.user.RelationshipState.ACTIVE;


@Service
@Slf4j
public class PartyConnectorImpl implements PartyConnector {

    protected static final String PRODUCT_ID_IS_REQUIRED = "A productId is required";
    protected static final String INSTITUTION_ID_IS_REQUIRED = "An institutionId is required ";
    protected static final String USER_ID_IS_REQUIRED = "A userId is required";
    private final PartyProcessRestClient restClient;
    private static final BinaryOperator<InstitutionInfo> MERGE_FUNCTION =
            (inst1, inst2) -> {
                if (inst1.getUserRole().compareTo(inst2.getUserRole()) < 0 ){
                    inst1.getProductRoles().addAll(inst2.getProductRoles());
                    return inst1;
                }else{
                    inst2.getProductRoles().addAll(inst1.getProductRoles());
                    return inst2;
                }
            };

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
        Set<String> productRole = new HashSet<>();
        productRole.add(onboardingData.getProductInfo().getRole());
        institutionInfo.setProductRoles(productRole);
        institutionInfo.setOrigin(onboardingData.getOrigin());
        institutionInfo.setOriginId(onboardingData.getOriginId());
        institutionInfo.setInstitutionType(onboardingData.getInstitutionType());
        institutionInfo.setUserRole(onboardingData.getRole());
        return institutionInfo;
    };

    private static final Function<RelationshipInfo, PartyProduct> RELATIONSHIP_INFO_TO_PARTY_PRODUCT_FUNCTION = relationshipInfo -> {
        PartyProduct partyProduct = new PartyProduct();
        partyProduct.setId(relationshipInfo.getProduct().getId());
        partyProduct.setRole(relationshipInfo.getRole());
        return partyProduct;
    };


    @Autowired
    public PartyConnectorImpl(PartyProcessRestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Collection<InstitutionInfo> getOnBoardedInstitutions(String productId) {
        log.trace("getOnBoardedInstitutions start");
        log.debug("getOnboardedInstitutions productId = {}", productId);
        Assert.hasText(productId, PRODUCT_ID_IS_REQUIRED);
        OnBoardingInfo onBoardingInfo = restClient.getOnBoardingInfo(null, EnumSet.of(ACTIVE));
        Collection<InstitutionInfo> result = parseOnBoardingInfo(onBoardingInfo, productId);
        log.debug("getOnBoardedInstitutions result = {}", result);
        log.trace("getOnBoardedInstitutions end");
        return result;
    }

    @Override
    public List<PartyProduct> getInstitutionUserProducts(String institutionId, String userId) {
        log.trace("getInstitutionUserProducts start");
        log.debug("getInstitutionUserProducts institutionId = {}, userId = {}", institutionId, userId);
        Assert.hasText(institutionId, INSTITUTION_ID_IS_REQUIRED);
        Assert.hasText(userId, USER_ID_IS_REQUIRED);
        List<PartyProduct> products = Collections.emptyList();
        RelationshipsResponse response = restClient.getUserInstitutionRelationships(institutionId, EnumSet.allOf(PartyRole.class), EnumSet.of(ACTIVE), null, null, userId);
        if (response != null) {
            products = response.stream()
                    .map(RELATIONSHIP_INFO_TO_PARTY_PRODUCT_FUNCTION)
                    .collect(Collectors.toList());
        }
        log.debug("getInstitutionUserProducts result = {}", products);
        log.trace("getInstitutionUserProducts start");
        return products;
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
