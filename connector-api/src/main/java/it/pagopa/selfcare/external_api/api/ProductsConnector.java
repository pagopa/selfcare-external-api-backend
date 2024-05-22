package it.pagopa.selfcare.external_api.api;

import it.pagopa.selfcare.onboarding.common.InstitutionType;
import it.pagopa.selfcare.product.entity.Product;

import java.util.List;


public interface ProductsConnector {

    List<Product> getProducts();

    Product getProduct(String productId);

    Product getProduct(String id, InstitutionType institutionType);
}
