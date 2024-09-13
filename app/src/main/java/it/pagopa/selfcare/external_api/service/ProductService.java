package it.pagopa.selfcare.external_api.service;


import it.pagopa.selfcare.product.entity.Product;

public interface ProductService {

    Product getProduct(String productId);
}
