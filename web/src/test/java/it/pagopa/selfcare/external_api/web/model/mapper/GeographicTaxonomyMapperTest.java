package it.pagopa.selfcare.external_api.web.model.mapper;

import it.pagopa.selfcare.external_api.model.institutions.GeographicTaxonomy;
import it.pagopa.selfcare.external_api.web.model.institutions.GeographicTaxonomyResource;
import org.junit.jupiter.api.Test;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.junit.jupiter.api.Assertions.*;

class GeographicTaxonomyMapperTest {


    @Test
    void toGeographicTaxonomyResource() {
        //given
        GeographicTaxonomy model = mockInstance(new GeographicTaxonomy());
        //when
        GeographicTaxonomyResource resource = GeographicTaxonomyMapper.toResource(model);
        //then
        assertNotNull(resource);
        assertEquals(model.getCode(), resource.getCode());
        assertEquals(model.getDesc(), resource.getDesc());
    }

    @Test
    void toGeographicTaxonomyResource_null() {
        //given
        GeographicTaxonomy model = null;
        //when
        GeographicTaxonomyResource resource = GeographicTaxonomyMapper.toResource(model);
        //then
        assertNull(resource);
    }

}