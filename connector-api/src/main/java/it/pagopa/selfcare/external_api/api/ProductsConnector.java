package it.pagopa.selfcare.external_api.api;

import it.pagopa.selfcare.external_api.model.product.Product;

import java.util.List;

public interface ProductsConnector {

    List<Product> getProducts();

}
