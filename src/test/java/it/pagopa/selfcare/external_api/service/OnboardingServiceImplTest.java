package it.pagopa.selfcare.external_api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.InstitutionResponse;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.InstitutionsResponse;
import it.pagopa.selfcare.external_api.client.MsCoreInstitutionApiClient;
import it.pagopa.selfcare.external_api.client.MsOnboardingControllerApi;
import it.pagopa.selfcare.external_api.client.MsUserApiRestClient;
import it.pagopa.selfcare.external_api.exception.ResourceNotFoundException;
import it.pagopa.selfcare.external_api.mapper.OnboardingMapper;
import it.pagopa.selfcare.external_api.mapper.OnboardingMapperImpl;
import it.pagopa.selfcare.external_api.mapper.UserResourceMapper;
import it.pagopa.selfcare.external_api.model.institution.Institution;
import it.pagopa.selfcare.external_api.model.onboarding.InstitutionUpdate;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingData;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingUsersRequest;
import it.pagopa.selfcare.external_api.model.user.RelationshipInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;

import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import static it.pagopa.selfcare.commons.base.utils.InstitutionType.*;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
class OnboardingServiceImplTest extends BaseServiceTestUtils {
    @InjectMocks
    private OnboardingServiceImpl onboardingService;

    @Mock
    private MsOnboardingControllerApi onboardingControllerApi;

    @Mock
    private MsCoreInstitutionApiClient institutionApiClient;

    @Mock
    private MsUserApiRestClient msUserApiRestClient;

    @Spy
    private UserResourceMapper userResourceMapper;

    @Spy
    private OnboardingMapperImpl onboardingMapper;

    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    void oldContractOnboardingTest(){
        OnboardingData onboardingData = new OnboardingData();
        onboardingData.setInstitutionExternalId("externalId");
        Assertions.assertDoesNotThrow(() -> onboardingService.oldContractOnboardingV2(onboardingData));
    }

    @Test
    void autoApprovalOnboardingProductV2TestPA(){
        OnboardingData onboardingData = new OnboardingData();
        onboardingData.setInstitutionExternalId("externalId");
        onboardingData.setInstitutionUpdate(new InstitutionUpdate());
        onboardingData.setInstitutionType(PA);
        Assertions.assertDoesNotThrow(() -> onboardingService.autoApprovalOnboardingProductV2(onboardingData));
    }

    @Test
    void autoApprovalOnboardingProductV2TestPSP(){
        OnboardingData onboardingData = new OnboardingData();
        onboardingData.setInstitutionExternalId("externalId");
        onboardingData.setInstitutionUpdate(new InstitutionUpdate());
        onboardingData.setInstitutionType(PSP);
        Assertions.assertDoesNotThrow(() -> onboardingService.autoApprovalOnboardingProductV2(onboardingData));
    }

    @Test
    void autoApprovalOnboardingProductV2Test(){
        OnboardingData onboardingData = new OnboardingData();
        onboardingData.setInstitutionUpdate(new InstitutionUpdate());
        onboardingData.setInstitutionExternalId("externalId");
        onboardingData.setInstitutionType(PG);
        Assertions.assertDoesNotThrow(() -> onboardingService.autoApprovalOnboardingProductV2(onboardingData));
    }

    @Test
    void onboardingUsers_noInstitutionFound() throws Exception {
        ClassPathResource inputResource = new ClassPathResource("expectations/OnboardingUsersRequest.json");
        byte[] onboardingUsersRequestStream = Files.readAllBytes(inputResource.getFile().toPath());
        OnboardingUsersRequest onboardingUsersRequest = objectMapper.readValue(onboardingUsersRequestStream, OnboardingUsersRequest.class);
        when(institutionApiClient._getInstitutionsUsingGET(onboardingUsersRequest.getInstitutionTaxCode(), onboardingUsersRequest.getInstitutionSubunitCode(), null, null))
                .thenReturn(ResponseEntity.ok(new InstitutionsResponse().institutions(Collections.emptyList())));
        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> onboardingService.onboardingUsers(onboardingUsersRequest, "userName", "surname"),
                "Institution not found for given value");
    }

    @Test
    void onboardingUsers_noInstitutionFound2() throws Exception {
        ClassPathResource inputResource = new ClassPathResource("expectations/OnboardingUsersRequest.json");
        byte[] onboardingUsersRequestStream = Files.readAllBytes(inputResource.getFile().toPath());
        OnboardingUsersRequest onboardingUsersRequest = objectMapper.readValue(onboardingUsersRequestStream, OnboardingUsersRequest.class);
        when(institutionApiClient._getInstitutionsUsingGET(onboardingUsersRequest.getInstitutionTaxCode(), onboardingUsersRequest.getInstitutionSubunitCode(), null, null))
                .thenReturn(ResponseEntity.ok(new InstitutionsResponse().institutions(Collections.emptyList())));
        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> onboardingService.onboardingUsers(onboardingUsersRequest, "userName", "surname"),
                "Institution not found for given value");
    }


    @Test
    void onboardingUsers_happyPath() throws Exception {
        ClassPathResource onboardingUsersRequestInputResource = new ClassPathResource("expectations/OnboardingUsersRequest.json");
        byte[] onboardingUsersRequestStream = Files.readAllBytes(onboardingUsersRequestInputResource.getFile().toPath());
        OnboardingUsersRequest onboardingUsersRequest = objectMapper.readValue(onboardingUsersRequestStream, OnboardingUsersRequest.class);

        ClassPathResource institutionInputResource = new ClassPathResource("expectations/InstitutionResponse.json");
        byte[] institutionStream = Files.readAllBytes(institutionInputResource.getFile().toPath());
        List<InstitutionResponse> institutions = objectMapper.readValue(institutionStream, new TypeReference<>() {
        });
        when(institutionApiClient._getInstitutionsUsingGET(onboardingUsersRequest.getInstitutionTaxCode(), onboardingUsersRequest.getInstitutionSubunitCode(), null, null))
                .thenReturn(ResponseEntity.ok(new InstitutionsResponse().institutions(institutions)));
        when(msUserApiRestClient._usersPost(any())).thenReturn(ResponseEntity.ok("userId"));
        when(msUserApiRestClient._usersUserIdPost(any(), any())).thenReturn(ResponseEntity.ok().build());
        List<RelationshipInfo> result = onboardingService.onboardingUsers(onboardingUsersRequest, "userName", "surname");

        assert result.size() == 2;
    }
}
