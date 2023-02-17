package it.pagopa.selfcare.external_api.web.model.mapper;

import it.pagopa.selfcare.external_api.model.institutions.*;
import it.pagopa.selfcare.external_api.model.onboarding.Billing;
import it.pagopa.selfcare.external_api.web.model.institutions.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static it.pagopa.selfcare.commons.utils.TestUtils.*;
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
        model.getDataProtectionOfficer().setEmail("dpoEmail@example.com");
        model.getDataProtectionOfficer().setPec("dpoPec@example.com");
        model.getAssistanceContacts().setSupportEmail("spportEmail@example.com");
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
        assertEquals(resource.getRecipientCode(), model.getBilling().getRecipientCode());
        reflectionEqualsByName(resource.getAssistanceContacts(), model.getAssistanceContacts());
        reflectionEqualsByName(resource.getCompanyInformations(), model.getCompanyInformations());
        reflectionEqualsByName(resource.getPspData(), model.getPaymentServiceProvider());
        reflectionEqualsByName(resource.getDpoData(), model.getDataProtectionOfficer());
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

    @Test
    void toResource_nullAssistanceContacts() {
        // given
        AssistanceContacts model = null;
        // when
        AssistanceContactsResource resource = InstitutionMapper.toResource(model);
        // then
        assertNull(resource);
    }

    @Test
    void toResource_nullCompanyInformations() {
        // given
        CompanyInformations model = null;
        // when
        CompanyInformationsResource resource = InstitutionMapper.toResource(model);
        // then
        assertNull(resource);
    }

    @Test
    void toResource_nullPspData() {
        // given
        PaymentServiceProvider model = null;
        // when
        PspDataResource resource = InstitutionMapper.toResource(model);
        // then
        assertNull(resource);
    }

    @Test
    void toResource_nullDpoData() {
        // given
        DataProtectionOfficer model = null;
        // when
        DpoDataResource resource = InstitutionMapper.toResource(model);
        // then
        assertNull(resource);
    }

}
