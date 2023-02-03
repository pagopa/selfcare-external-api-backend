package it.pagopa.selfcare.external_api.web.model.mapper;

import it.pagopa.selfcare.external_api.model.onboarding.GeographicTaxonomy;
import it.pagopa.selfcare.external_api.web.model.onboarding.GeographicTaxonomyDto;
import org.junit.jupiter.api.Test;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.junit.jupiter.api.Assertions.*;

class GeographicTaxonomyMapperTest {

    @Test
    void toGeographicTaxonomy() {
        //given
        GeographicTaxonomyDto model = mockInstance(new GeographicTaxonomyDto());
        //when
        GeographicTaxonomy resource = GeographicTaxonomyMapper.fromDto(model);
        //then
        assertNotNull(resource);
        assertEquals(model.getCode(), resource.getCode());
        assertEquals(model.getDesc(), resource.getDesc());
    }

    @Test
    void toGeographicTaxonomy_null() {
        //given
        GeographicTaxonomyDto model = null;
        //when
        GeographicTaxonomy resource = GeographicTaxonomyMapper.fromDto(model);
        //then
        assertNull(resource);
    }

}
