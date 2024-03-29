package it.pagopa.selfcare.external_api.api;

import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import it.pagopa.selfcare.external_api.model.product.Product;

import java.util.List;


public interface ProductsConnector {

    List<Product> getProducts();

    Product getProduct(String productId);

    Product getProduct(String id, InstitutionType institutionType);
}
