package it.pagopa.selfcare.external_api.service;

import it.pagopa.selfcare.product.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final it.pagopa.selfcare.product.service.ProductService productService;

    @Override
    public Product getProduct(String productId) {
        log.trace("getProduct start");
        log.debug("getProduct productId = {}", productId);
        Product result = productService.getProduct(productId);
        log.debug("getProduct result = {}", result);
        log.trace("getProduct end");
        return result;
    }
}
