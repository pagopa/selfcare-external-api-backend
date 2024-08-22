package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.external_api.api.*;
import it.pagopa.selfcare.external_api.model.institutions.GeographicTaxonomy;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.institutions.SearchMode;
import it.pagopa.selfcare.external_api.model.nationalRegistries.LegalVerification;
import it.pagopa.selfcare.external_api.model.pnpg.CreatePnPgInstitution;
import it.pagopa.selfcare.external_api.model.user.OnboardedProductResponse;
import it.pagopa.selfcare.external_api.model.user.User;
import it.pagopa.selfcare.external_api.model.user.UserInstitution;
import it.pagopa.selfcare.external_api.model.user.UserProductResponse;
import it.pagopa.selfcare.product.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.owasp.encoder.Encode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.external_api.model.user.RelationshipState.ACTIVE;
import static it.pagopa.selfcare.external_api.model.user.User.Fields.*;

@Service
@Slf4j
class InstitutionServiceImpl implements InstitutionService {

    private static final EnumSet<User.Fields> USER_FIELD_LIST = EnumSet.of(name, familyName, workContacts);
    private static final EnumSet<User.Fields> USER_FIELD_LIST_FISCAL_CODE = EnumSet.of(name, familyName, workContacts, fiscalCode);
    static final String REQUIRED_INSTITUTION_MESSAGE = "An Institution id is required";
    private final ProductsConnector productsConnector;
    private final MsCoreConnector msCoreConnector;
    private final UserRegistryConnector userRegistryConnector;
    private final UserMsConnector userMsConnector;
    private static final String TAG_LOG_INSTITUTION_USER_PRODUCTS = "getInstitutionUserProducts";
    private final Set<String> serviceType;

    private final MsPartyRegistryProxyConnector msPartyRegistryProxyConnector;

    @Autowired
    InstitutionServiceImpl(ProductsConnector productsConnector,
                           MsCoreConnector msCoreConnector, UserRegistryConnector userRegistryConnector,
                           UserMsConnector userMsConnector, @Value("${external_api.allowed-service-types}")String[] serviceType, MsPartyRegistryProxyConnector msPartyRegistryProxyConnector) {
        this.productsConnector = productsConnector;
        this.msCoreConnector = msCoreConnector;
        this.userRegistryConnector = userRegistryConnector;
        this.userMsConnector = userMsConnector;
        this.serviceType = Set.of(serviceType);
        this.msPartyRegistryProxyConnector = msPartyRegistryProxyConnector;
    }


    @Override
    public List<Product> getInstitutionUserProductsV2(String institutionId, String userId) {
        log.trace(TAG_LOG_INSTITUTION_USER_PRODUCTS + " start");
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_MESSAGE);

        List<Product> products = productsConnector.getProducts();
        if (!products.isEmpty()) {
            List<String> productIds = msCoreConnector.getInstitutionUserProductsV2(institutionId, userId);
            products = products.stream()
                    .filter(product -> productIds.contains(product.getId()))
                    .toList();
        }
        log.trace(TAG_LOG_INSTITUTION_USER_PRODUCTS + " result = {}", products);
        log.trace(TAG_LOG_INSTITUTION_USER_PRODUCTS + " end");
        return products;
    }

    @Override
    public Collection<UserProductResponse> getInstitutionProductUsersV2(String institutionId, String productId, String userId, Set<String> productRoles, String xSelfCareUid) {
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_MESSAGE);
        Assert.hasText(productId, "A Product id is required");
        List<String> productRolesList = CollectionUtils.isEmpty(productRoles) ? null : new ArrayList<>(productRoles);

        List<UserInstitution> usersInstitutions = userMsConnector.getUsersInstitutions(userId, institutionId, null, null, productRolesList, List.of(productId), null, List.of(ACTIVE.name()));
        if(Objects.isNull(usersInstitutions) || usersInstitutions.isEmpty()) return List.of();
        Collection<UserProductResponse> userProductResponses = usersInstitutions.stream()
                .filter(userInstitution -> !CollectionUtils.isEmpty(userInstitution.getProducts()))
                .map(userInstitution -> {
                    UserProductResponse userProduct = new UserProductResponse();
                    userProduct.setUserMailUuid(userInstitution.getUserMailUuid());
                    userProduct.setUser(retrieveUserFromUserRegistry(userInstitution, xSelfCareUid));
                    userProduct.setRole(evaluateRoles(userInstitution));
                    userProduct.setRoles(userInstitution.getProducts().stream().map(OnboardedProductResponse::getProductRole).toList());
                    return userProduct;
                })
                .collect(Collectors.toList());

        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutionProductUsers result = {}", userProductResponses);
        log.trace("getInstitutionProductUsers end");
        return userProductResponses;
    }

    private PartyRole evaluateRoles(UserInstitution userInstitution) {
        List<PartyRole> roles = userInstitution.getProducts().stream()
                .map(OnboardedProductResponse::getRole)
                .filter(role -> Arrays.stream(PartyRole.values()).anyMatch(partyRole -> partyRole.name().equals(role)))
                .map(PartyRole::valueOf)
                .toList();

        return CollectionUtils.isEmpty(roles) ? null : Collections.min(roles);
    }

    private User retrieveUserFromUserRegistry(UserInstitution userInstitution, String xSelfCareUid) {
        if (StringUtils.hasText(xSelfCareUid) && serviceType.contains(xSelfCareUid)) {
            return userRegistryConnector.getUserByInternalId(userInstitution.getUserId(), USER_FIELD_LIST_FISCAL_CODE);
        } else {
            return userRegistryConnector.getUserByInternalId(userInstitution.getUserId(), USER_FIELD_LIST);
        }
    }

    @Override
    public List<GeographicTaxonomy> getGeographicTaxonomyList(String institutionId) {
        log.trace("getGeographicTaxonomyList start");
        log.debug("getGeographicTaxonomyList externalInstitutionId = {}", institutionId);
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_MESSAGE);
        List<GeographicTaxonomy> result = msCoreConnector.getGeographicTaxonomyList(institutionId);
        log.debug("getGeographicTaxonomyList result = {}", result);
        log.trace("getGeographicTaxonomyList end");
        return result;
    }

    @Override
    public Collection<Institution> getInstitutionsByGeoTaxonomies(Set<String> geoTaxIds, SearchMode searchMode) {
        log.trace("getInstitutionByGeoTaxonomy start");
        log.debug("getInstitutionByGeoTaxonomy geoTaxIds = {}, searchMode = {}", geoTaxIds, searchMode);
        Assert.notEmpty(geoTaxIds, "GeoTaxonomy ids are required in order to retrieve the institutions");
        String geoIds = String.join(",", geoTaxIds);
        Collection<Institution> institutions = msCoreConnector.getInstitutionsByGeoTaxonomies(geoIds, searchMode);
        log.debug("getInstitutionByGeoTaxonomy result = {}", "null)");
        log.trace("getInstitutionByGeoTaxonomy end");
        return institutions;
    }

    @Override
    public String addInstitution(CreatePnPgInstitution request) {
        log.trace("addInstitution start");
        log.debug("addInstitution request = {}", request);
        String institutionInternalId = msCoreConnector.createPgInstitution(request.getDescription(), request.getExternalId());
        log.debug("addInstitution result = {}", institutionInternalId);
        log.trace("addInstitution end");
        return institutionInternalId;
    }

    @Override
    public LegalVerification verifyLegal(String taxId, String vatNumber) {
        log.trace("verifyLegal start");
        log.debug("verifyLegal taxId = {}, vatNumber = {}", Encode.forJava(taxId), Encode.forJava(vatNumber));
        LegalVerification legalVerification = msPartyRegistryProxyConnector.verifyLegal(taxId, vatNumber);
        log.trace("verifyLegal end");
        return legalVerification;
    }

}
