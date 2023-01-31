package it.pagopa.selfcare.external_api.web.model.mapper;

import it.pagopa.selfcare.external_api.model.onboarding.ImportContract;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingImportData;
import it.pagopa.selfcare.external_api.web.model.onboarding.ImportContractDto;
import it.pagopa.selfcare.external_api.web.model.onboarding.OnboardingImportDto;
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
        ImportContract resource = OnboardingMapper.fromDto(model);
        //then
        assertNotNull(resource);
        reflectionEqualsByName(model, resource);
    }

    @Test
    void fromDtoImportContract_null() {
        // given
        ImportContractDto importContractDto = null;
        // when
        ImportContract resource = OnboardingMapper.fromDto(importContractDto);
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
        assertEquals("prod-io", resource.getProductId());
        reflectionEqualsByName(userDtos.get(0), resource.getUsers().get(0));
        reflectionEqualsByName(importContractDto, resource.getImportContract());
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
}
