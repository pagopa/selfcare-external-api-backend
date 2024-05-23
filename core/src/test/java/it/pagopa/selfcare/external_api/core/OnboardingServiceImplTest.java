package it.pagopa.selfcare.external_api.core;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.external_api.api.OnboardingMsConnector;
import it.pagopa.selfcare.external_api.api.UserMsConnector;
import it.pagopa.selfcare.external_api.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
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
    private OnboardingMsConnector onboardingMsConnectorMock;
    @Mock
    private UserMsConnector userMsConnectorMock;

    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    void oldContractOnboardingTest(){
        OnboardingData onboardingData = new OnboardingData();
        onboardingData.setInstitutionExternalId("externalId");
        doNothing().when(onboardingMsConnectorMock).onboardingImportPA(onboardingData);
        Assertions.assertDoesNotThrow(() -> onboardingService.oldContractOnboardingV2(onboardingData));
        verify(onboardingMsConnectorMock).onboardingImportPA(onboardingData);
    }

    @Test
    void autoApprovalOnboardingProductV2Test(){
        OnboardingData onboardingData = new OnboardingData();
        onboardingData.setInstitutionExternalId("externalId");
        doNothing().when(onboardingMsConnectorMock).onboarding(onboardingData);
        Assertions.assertDoesNotThrow(() -> onboardingService.autoApprovalOnboardingProductV2(onboardingData));
        verify(onboardingMsConnectorMock).onboarding(onboardingData);
    }

    @Test
    void onboardingUsers_noInstitutionFound() throws Exception {
        ClassPathResource inputResource = new ClassPathResource("expectations/OnboardingUsersRequest.json");
        byte[] onboardingUsersRequestStream = Files.readAllBytes(inputResource.getFile().toPath());
        OnboardingUsersRequest onboardingUsersRequest = objectMapper.readValue(onboardingUsersRequestStream, OnboardingUsersRequest.class);
        when(onboardingMsConnectorMock.getInstitutionsByTaxCodeAndSubunitCode(onboardingUsersRequest.getInstitutionTaxCode(), onboardingUsersRequest.getInstitutionSubunitCode()))
                .thenReturn(Collections.emptyList());
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

        when(onboardingMsConnectorMock.getInstitutionsByTaxCodeAndSubunitCode(onboardingUsersRequest.getInstitutionTaxCode(), onboardingUsersRequest.getInstitutionSubunitCode())).thenReturn(institutions);
        when(userMsConnectorMock.addUserRole("userId", institutions.get(0), "productId", "MANAGER", List.of("operator"))).thenReturn("userId");
        when(userMsConnectorMock.createUser(institutions.get(0), "productId", "MANAGER", List.of("admin"), onboardingUsersRequest.getUsers().get(0), true)).thenReturn("userId2");

        List<RelationshipInfo> result = onboardingService.onboardingUsers(onboardingUsersRequest, "userName", "surname");
        (Mockito.verify(onboardingMsConnectorMock, Mockito.times(1))).getInstitutionsByTaxCodeAndSubunitCode(onboardingUsersRequest.getInstitutionTaxCode(), onboardingUsersRequest.getInstitutionSubunitCode());

        assert result.size() == 2;
    }
}
