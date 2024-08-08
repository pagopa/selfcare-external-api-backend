package it.pagopa.selfcare.external_api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.external_api.client.MsCoreInstitutionApiClient;
import it.pagopa.selfcare.external_api.client.MsOnboardingControllerApi;
import it.pagopa.selfcare.external_api.client.MsUserApiRestClient;
import it.pagopa.selfcare.external_api.exception.ResourceNotFoundException;
import it.pagopa.selfcare.external_api.mapper.OnboardingMapper;
import it.pagopa.selfcare.external_api.mapper.UserResourceMapper;
import it.pagopa.selfcare.external_api.model.institution.Institution;
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

import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
class OnboardingServiceImplTest extends BaseServiceTestUtils {
    @InjectMocks
    private OnboardingServiceImpl onboardingService;

    @Mock
    private MsOnboardingControllerApi onboardingControllerApi;

    @Spy
    private OnboardingMapper onboardingMapper;

    @Mock
    private MsCoreInstitutionApiClient institutionApiClient;

    @Mock
    private MsUserApiRestClient msUserApiRestClient;

    @Spy
    private UserResourceMapper userResourceMapper;

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
    void autoApprovalOnboardingProductV2Test(){
        OnboardingData onboardingData = new OnboardingData();
        onboardingData.setInstitutionExternalId("externalId");
        Assertions.assertDoesNotThrow(() -> onboardingService.autoApprovalOnboardingProductV2(onboardingData));
    }

    @Test
    void onboardingUsers_noInstitutionFound() throws Exception {
        ClassPathResource inputResource = new ClassPathResource("expectations/OnboardingUsersRequest.json");
        byte[] onboardingUsersRequestStream = Files.readAllBytes(inputResource.getFile().toPath());
        OnboardingUsersRequest onboardingUsersRequest = objectMapper.readValue(onboardingUsersRequestStream, OnboardingUsersRequest.class);
        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> onboardingService.onboardingUsers(onboardingUsersRequest, "userName", "surname"),
                "Institution not found for given value");
    }


    @Test
    void onboardingUsers_happyPath() throws Exception {
        ClassPathResource onboardingUsersRequestInputResource = new ClassPathResource("expectations/OnboardingUsersRequest.json");
        byte[] onboardingUsersRequestStream = Files.readAllBytes(onboardingUsersRequestInputResource.getFile().toPath());
        OnboardingUsersRequest onboardingUsersRequest = objectMapper.readValue(onboardingUsersRequestStream, OnboardingUsersRequest.class);

        ClassPathResource institutionInputResource = new ClassPathResource("expectations/Institution.json");
        byte[] institutionStream = Files.readAllBytes(institutionInputResource.getFile().toPath());
        List<Institution> institutions = objectMapper.readValue(institutionStream, new TypeReference<>() {
        });

        List<RelationshipInfo> result = onboardingService.onboardingUsers(onboardingUsersRequest, "userName", "surname");

        assert result.size() == 2;
    }
}
