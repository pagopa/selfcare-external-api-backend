package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.external_api.api.PartyConnector;
import it.pagopa.selfcare.external_api.api.ProductsConnector;
import it.pagopa.selfcare.external_api.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.external_api.model.product.PartyProduct;
import it.pagopa.selfcare.external_api.model.product.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
class InstitutionServiceImpl implements InstitutionService {

    private final PartyConnector partyConnector;
    private final ProductsConnector productsConnector;

    @Autowired
    InstitutionServiceImpl(PartyConnector partyConnector, ProductsConnector productsConnector) {
        this.partyConnector = partyConnector;
        this.productsConnector = productsConnector;
    }

    @Override
    public Collection<InstitutionInfo> getInstitutions(String productId) {
        log.trace("getInstitutions start");
        log.debug("getInstitutions productId = {}", productId);
        Collection<InstitutionInfo> result = partyConnector.getOnBoardedInstitutions(productId);
        log.debug("getInstitutions result = {}", result);
        log.trace("getInstitutions end");
        return result;
    }

    @Override
    public List<Product> getInstitutionUserProducts(String institutionId) {
        log.trace("getInstitutionUserProducts start");
        log.debug("getInstitutionUserProducts institutionId = {}", institutionId);
        Assert.hasText(institutionId, "An institutionId is required");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Assert.state(authentication != null, "Authentication is required");
        Assert.state(authentication.getPrincipal() instanceof SelfCareUser, "Not SelfCareUser principal");
        SelfCareUser user = (SelfCareUser) authentication.getPrincipal();
        List<Product> products = productsConnector.getProducts();
        if (!products.isEmpty()) {
            Map<String, PartyProduct> institutionUserProducts = partyConnector.getInstitutionUserProducts(institutionId, user.getId()).stream()
                    .collect(Collectors.toMap(PartyProduct::getId, Function.identity(), (partyProduct, partyProduct2) ->  partyProduct));
            products = products.stream()
                    .filter(product -> institutionUserProducts.containsKey(product.getId()))
                    .collect(Collectors.toList());
        }
        log.debug("getInstitutionUserProducts result = {}", products);
        log.trace("getInstitutionUserProducts end");
        return products;
    }
}
