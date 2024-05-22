package it.pagopa.selfcare.external_api.connector.rest;

import it.pagopa.selfcare.external_api.api.ProductsConnector;
import it.pagopa.selfcare.onboarding.common.InstitutionType;
import it.pagopa.selfcare.product.entity.Product;
import it.pagopa.selfcare.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ProductConnectorImpl implements ProductsConnector {

    private final ProductService productService;

    public ProductConnectorImpl(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public List<Product> getProducts() {
        return productService.getProducts(true, true);
    }

    @Override
    public Product getProduct(String productId) {
        return productService.getProduct(productId);
    }

    @Override
    public Product getProduct(String id, InstitutionType institutionType) {
        Product product = productService.getProduct(id);
        if (product.getInstitutionContractMappings().containsKey(institutionType)) {
            product.setContractTemplatePath(product.getInstitutionContractMappings().get(institutionType).getContractTemplatePath());
            product.setContractTemplateVersion(product.getInstitutionContractMappings().get(institutionType).getContractTemplateVersion());
            product.setContractTemplateUpdatedAt(product.getInstitutionContractMappings().get(institutionType).getContractTemplateUpdatedAt());
        }
        return product;
    }


}
