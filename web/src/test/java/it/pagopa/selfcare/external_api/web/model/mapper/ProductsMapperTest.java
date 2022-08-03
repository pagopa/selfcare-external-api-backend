package it.pagopa.selfcare.external_api.web.model.mapper;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.external_api.model.product.Product;
import it.pagopa.selfcare.external_api.web.model.products.ProductResource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProductsMapperTest {
    
    @Test
    void toResource_notNull() {
        // given
        Product product = TestUtils.mockInstance(new Product());
        // when
        ProductResource resource = ProductsMapper.toResource(product);
        // then
        assertEquals(product.getId(), resource.getId());
        assertEquals(product.getTitle(), resource.getTitle());
        assertEquals(product.getDescription(), resource.getDescription());
        assertEquals(product.getUrlPublic(), resource.getUrlPublic());
        assertEquals(product.getUrlBO(), resource.getUrlBO());
        TestUtils.reflectionEqualsByName(product, resource);
    }

    @Test
    void toResource_null() {
        // given
        Product model = null;
        // when
        ProductResource resource = ProductsMapper.toResource(model);
        // then
        assertNull(resource);
    }

}
