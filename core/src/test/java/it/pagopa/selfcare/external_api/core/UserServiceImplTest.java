package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.external_api.api.MsCoreConnector;
import it.pagopa.selfcare.external_api.api.UserRegistryConnector;
import it.pagopa.selfcare.external_api.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionResponse;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingInfoResponse;
import it.pagopa.selfcare.external_api.model.user.Certification;
import it.pagopa.selfcare.external_api.model.user.CertifiedField;
import it.pagopa.selfcare.external_api.model.user.User;
import it.pagopa.selfcare.external_api.model.user.UserInfoWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.test.context.TestSecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRegistryConnector userRegistryConnector;

    @Mock
    private MsCoreConnector msCoreConnector;

    private static final String fiscalCode = "MNCCSD01R13A757G";

    @BeforeEach
    void beforeEach() {
        TestSecurityContextHolder.clearContext();
    }

    private final static User dummyUser;

    static {
        dummyUser = new User();
        dummyUser.setFiscalCode("MNCCSD01R13A757G");
        dummyUser.setId("id");
        CertifiedField<String> fieldName = new CertifiedField<>();
        fieldName.setCertification(Certification.SPID);
        fieldName.setValue("testName");
        CertifiedField<String> fieldEmail = new CertifiedField<>();
        fieldEmail.setCertification(Certification.SPID);
        fieldEmail.setValue("test@email.com");
        dummyUser.setEmail(fieldEmail);
        dummyUser.setName(fieldName);
    }

    @Test
    void getUserInfo() {
        //given
        Optional<User> optUser = Optional.of(dummyUser);
        // when
        when(userRegistryConnector.search(anyString(), any()))
                .thenReturn(optUser);
        OnboardingInfoResponse onboardingInfoResponse = buildOnboardingInfoResponse();
        when(msCoreConnector.getInstitutionProductsInfo("id")).thenReturn(onboardingInfoResponse);
        UserInfoWrapper userWrapper = userService.getUserInfo(fiscalCode);
        // then
        assertNotNull(userWrapper);
        assertNotNull(userWrapper.getUser());
        assertNotNull(userWrapper.getOnboardedInstitutions());
        assertEquals(1, userWrapper.getOnboardedInstitutions().size());
        assertEquals(optUser.get().getName().getValue(),  userWrapper.getUser().getName().getValue());
        assertEquals(optUser.get().getEmail().getValue(),  userWrapper.getUser().getEmail().getValue());
        assertEquals(onboardingInfoResponse.getInstitutions().get(0).getAddress(), userWrapper.getOnboardedInstitutions().get(0).getAddress());

    }

    @Test
    void getEmptyUser() {
        //given
        Optional<User> optUser = Optional.empty();
        // when
        when(userRegistryConnector.search(anyString(), any()))
                .thenReturn(optUser);

        Executable executable = () -> userService.getUserInfo(fiscalCode);
        // then
        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, executable);
        assertEquals("User with fiscal code " + fiscalCode + " not found", e.getMessage());

    }

    private OnboardingInfoResponse buildOnboardingInfoResponse() {
        OnboardingInfoResponse response = new OnboardingInfoResponse();
        OnboardedInstitutionResponse institutionResponse = new OnboardedInstitutionResponse();
        institutionResponse.setId("id");
        institutionResponse.setAddress("address");
        response.setInstitutions(List.of(institutionResponse));
        return response;
    }
}
