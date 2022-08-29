package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.external_api.api.ProductsConnector;
import it.pagopa.selfcare.external_api.model.product.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductsConnector productsConnector;

    @Autowired
    ProductServiceImpl(ProductsConnector productsConnector) {
        this.productsConnector = productsConnector;
    }

    @Override
    public Product getProduct(String productId) {
        log.trace("getProduct start");
        log.debug("getProduct productId = {}", productId);
        Product result = productsConnector.getProduct(productId);
        log.debug("getProduct result = {}", result);
        log.trace("getProduct end");
        return result;
    }
}
