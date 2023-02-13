package it.pagopa.selfcare.external_api.web.model.mapper;

import it.pagopa.selfcare.external_api.model.onboarding.Billing;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingData;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingImportContract;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingImportData;
import it.pagopa.selfcare.external_api.web.model.institutions.GeographicTaxonomyDto;
import it.pagopa.selfcare.external_api.web.model.onboarding.*;
import it.pagopa.selfcare.external_api.web.model.user.UserDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.commons.utils.TestUtils.reflectionEqualsByName;
import static org.junit.jupiter.api.Assertions.*;

class OnboardingMapperTest {

    @Test
    void fromDtoImportContract() {
        //given
        ImportContractDto model = mockInstance(new ImportContractDto());
        //when
        OnboardingImportContract resource = OnboardingMapper.fromDto(model);
        //then
        assertNotNull(resource);
        reflectionEqualsByName(model, resource);
    }

    @Test
    void fromDtoImportContract_null() {
        // given
        ImportContractDto importContractDto = null;
        // when
        OnboardingImportContract resource = OnboardingMapper.fromDto(importContractDto);
        // then
        assertNull(resource);
    }

    @Test
    void toOnboardingImportData() {
        //given
        String institutionId = "institutionId";
        List<UserDto> userDtos = List.of(mockInstance(new UserDto()));
        ImportContractDto importContractDto = mockInstance(new ImportContractDto());
        OnboardingImportDto model = mockInstance(new OnboardingImportDto());
        model.setUsers(userDtos);
        model.setImportContract(importContractDto);
        //when
        OnboardingImportData resource = OnboardingMapper.toOnboardingImportData(institutionId, model);
        //then
        assertNotNull(resource);
        assertEquals(model.getUsers().size(), resource.getUsers().size());
        assertEquals(institutionId, resource.getInstitutionExternalId());
        assertTrue(resource.getInstitutionUpdate().getImported());
        assertEquals("prod-io", resource.getProductId());
        reflectionEqualsByName(userDtos.get(0), resource.getUsers().get(0));
        reflectionEqualsByName(importContractDto, resource.getContractImported());
    }

    @Test
    void toOnboardingImportData_null() {
        //given
        String institutionId = "institutionId";
        OnboardingImportDto onboardingDto = null;
        //when
        OnboardingImportData resource = OnboardingMapper.toOnboardingImportData(institutionId, onboardingDto);
        //then
        assertNull(resource);
    }

    @Test
    void fromDto() {
        //given
        BillingDataDto model = mockInstance(new BillingDataDto());
        //when
        Billing resource = OnboardingMapper.fromDto(model);
        //then
        assertNotNull(resource);
        reflectionEqualsByName(model, resource);
    }

    @Test
    void fromDto_null() {
        //given
        BillingDataDto model = null;
        //when
        Billing resource = OnboardingMapper.fromDto(model);
        //then
        assertNull(resource);
    }

    @Test
    void toOnboardingData() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        List<UserDto> userDtos = List.of(mockInstance(new UserDto()));
        OnboardingDto model = mockInstance(new OnboardingDto());
        BillingDataDto billingDataDto = mockInstance(new BillingDataDto());
        PspDataDto pspDataDto = mockInstance(new PspDataDto());
        List<GeographicTaxonomyDto> geographicTaxonomyDtos = List.of(mockInstance(new GeographicTaxonomyDto()));
        model.setBillingData(billingDataDto);
        model.setUsers(userDtos);
        model.setPspData(pspDataDto);
        model.setGeographicTaxonomies(geographicTaxonomyDtos);
        //when
        OnboardingData resource = OnboardingMapper.toOnboardingData(institutionId, productId, model);
        //then
        assertNotNull(resource);
        assertEquals(model.getUsers().size(), resource.getUsers().size());
        assertEquals(model.getGeographicTaxonomies().size(), resource.getInstitutionUpdate().getGeographicTaxonomies().size());
        assertEquals(institutionId, resource.getInstitutionExternalId());
        assertEquals(productId, resource.getProductId());
        reflectionEqualsByName(billingDataDto, resource.getBilling());
        reflectionEqualsByName(userDtos.get(0), resource.getUsers().get(0));
        reflectionEqualsByName(model.getBillingData(), resource.getInstitutionUpdate());
        reflectionEqualsByName(model.getPspData(), resource.getInstitutionUpdate().getPaymentServiceProvider(), "dpoData");
        reflectionEqualsByName(model.getPspData().getDpoData(), resource.getInstitutionUpdate().getDataProtectionOfficer());
        reflectionEqualsByName(geographicTaxonomyDtos.get(0), resource.getInstitutionUpdate().getGeographicTaxonomies().get(0));
        assertEquals(model.getOrigin(), resource.getOrigin());
        assertEquals(model.getInstitutionType(), resource.getInstitutionType());
        assertEquals(model.getPricingPlan(), resource.getPricingPlan());
    }
}
