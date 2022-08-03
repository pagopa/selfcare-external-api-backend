package it.pagopa.selfcare.external_api.web.model.mapper;

import it.pagopa.selfcare.external_api.model.product.Product;
import it.pagopa.selfcare.external_api.web.model.products.ProductResource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductsMapper {

    public static ProductResource toResource(Product model){
        ProductResource resource = null;
        if(model != null){
            resource = new ProductResource();
            resource.setId(model.getId());
            resource.setDescription(model.getDescription());
            resource.setTitle(model.getTitle());
            resource.setUrlPublic(model.getUrlPublic());
            resource.setUrlBO(model.getUrlBO());
        }
        return resource;
    }
}
