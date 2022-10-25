package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.external_api.model.product.Product;

public interface ProductService {

    Product getProduct(String productId);
}
