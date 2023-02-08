package it.pagopa.selfcare.external_api.web.model.mapper;

import it.pagopa.selfcare.external_api.model.institutions.GeographicTaxonomy;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.external_api.model.onboarding.Billing;
import it.pagopa.selfcare.external_api.web.model.institutions.InstitutionDetailResource;
import it.pagopa.selfcare.external_api.web.model.institutions.InstitutionResource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static it.pagopa.selfcare.commons.utils.TestUtils.checkNotNullFields;
import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;

class InstitutionMapperTest {

    @Test
    void toResource_institutionInfo() {
        //given
        InstitutionInfo model = mockInstance(new InstitutionInfo(), "setId");
        model.setId(randomUUID().toString());
        model.setBilling(mockInstance(new Billing()));
        model.setProductRoles(List.of("string"));
        //when
        InstitutionResource resource = InstitutionMapper.toResource(model);
        //then
        assertNotNull(resource);
        assertEquals(resource.getId().toString(), model.getId());
        assertEquals(resource.getExternalId(), model.getExternalId());
        assertEquals(resource.getDescription(), model.getDescription());
        assertEquals(resource.getAddress(), model.getAddress());
        assertEquals(resource.getDigitalAddress(), model.getDigitalAddress());
        assertEquals(resource.getZipCode(), model.getZipCode());
        assertEquals(resource.getStatus(), model.getStatus());
        assertEquals(resource.getTaxCode(), model.getTaxCode());
        assertEquals(resource.getOrigin(), model.getOrigin());
        assertEquals(resource.getUserProductRoles(), model.getProductRoles());

    }

    @Test
    void toResource_nullInstitutionInfo() {
        //given
        final InstitutionInfo model = null;
        //when
        InstitutionResource resource = InstitutionMapper.toResource(model);
        //then
        assertNull(resource);
    }


    @Test
    void toResource_institution() {
        //given
        Institution model = mockInstance(new Institution(), "setId");
        model.setId(randomUUID().toString());
        model.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        //when
        InstitutionDetailResource result = InstitutionMapper.toResource(model);
        //then
        assertNotNull(result);
        checkNotNullFields(result);
    }

    @Test
    void toResource_nulInstitution() {
        //given+
        Institution model = null;
        //when
        InstitutionDetailResource resource = InstitutionMapper.toResource(model);
        //then
        assertNull(resource);
    }
}
