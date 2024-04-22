package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import it.pagopa.selfcare.external_api.api.*;
import it.pagopa.selfcare.external_api.core.strategy.OnboardingValidationStrategy;
import it.pagopa.selfcare.external_api.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.onboarding.User;
import it.pagopa.selfcare.external_api.model.onboarding.*;
import it.pagopa.selfcare.external_api.model.user.RelationshipInfo;
import it.pagopa.selfcare.external_api.model.user.UserToOnboard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.test.context.TestSecurityContextHolder;
import java.util.*;
import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OnboardingServiceImplTest {

    @InjectMocks
    private OnboardingServiceImpl onboardingServiceImpl;

    @Mock
    private MsCoreConnector partyConnectorMock;

    @Mock
    private ProductsConnector productsConnectorMock;

    @Mock
    private OnboardingValidationStrategy onboardingValidationStrategyMock;

    @Mock
    private UserRegistryConnector userRegistryConnectorMock;

    @Mock
    private MsPartyRegistryProxyConnector msPartyRegistryProxyConnectorMock;

    @Mock
    private OnboardingMsConnector onboardingMsConnectorMock;

    @Mock
    private UserMsConnector userMsConnector;

    @Captor
    private ArgumentCaptor<OnboardingImportData> onboardingImportDataCaptor;

    @Captor
    private ArgumentCaptor<OnboardingData> onboardingDataCaptor;

    private final static User dummyManager;
    private final static User dummyDelegate;

    @BeforeEach
    void beforeEach() {
        TestSecurityContextHolder.clearContext();
    }

    static {
        dummyManager = new User();
        dummyManager.setEmail("manager@pec.it");
        dummyManager.setName("manager");
        dummyManager.setSurname("manager");
        dummyManager.setTaxCode("manager");
        dummyManager.setRole(PartyRole.MANAGER);


        dummyDelegate = new User();
        dummyDelegate.setEmail("delegate@pec.it");
        dummyDelegate.setName("delegate");
        dummyDelegate.setSurname("delegate");
        dummyDelegate.setTaxCode("delegate");
        dummyDelegate.setRole(PartyRole.DELEGATE);
    }


    @Test
    void onboardingProductAsync() {
        // given
        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setInstitutionType", "setUsers");
        onboardingData.setInstitutionType(InstitutionType.PA);
        onboardingData.setUsers(List.of(dummyManager, dummyDelegate));
        // when
        onboardingServiceImpl.autoApprovalOnboardingProductV2(onboardingData);
        // then
        verify(onboardingMsConnectorMock, times(1))
                .onboarding(any());
    }

    @Test
    void onboardingImportPa() {
        // given
        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setInstitutionType", "setUsers");
        onboardingData.setUsers(List.of(dummyManager, dummyDelegate));
        // when
        onboardingServiceImpl.oldContractOnboardingV2(onboardingData);
        // then
        verify(onboardingMsConnectorMock, times(1))
                .onboardingImportPA(any());
    }

    @Test
    void onboardingUsers_happyPath() {
        OnboardingUsersRequest request = new OnboardingUsersRequest();
        UserToOnboard userToOnboard = new UserToOnboard();
        userToOnboard.setId("id");
        userToOnboard.setProductRole("admin");
        userToOnboard.setRole(PartyRole.MANAGER);

        UserToOnboard userToOnboard2 = new UserToOnboard();
        userToOnboard2.setTaxCode("taxCode");
        userToOnboard2.setProductRole("admin");
        userToOnboard2.setRole(PartyRole.MANAGER);

        UserToOnboard userToOnboard3 = new UserToOnboard();
        userToOnboard3.setTaxCode("taxCode");
        userToOnboard3.setProductRole("operator");
        userToOnboard3.setRole(PartyRole.MANAGER);

        request.setInstitutionTaxCode("taxCode");
        request.setInstitutionSubunitCode("subunitCode");
        request.setUsers(List.of(userToOnboard, userToOnboard2, userToOnboard3));

        Institution institution = new Institution();
        institution.setId("institutionId");

        when(onboardingMsConnectorMock.getInstitutionsByTaxCodeAndSubunitCode(anyString(), anyString())).thenReturn(List.of(institution));
        when(userMsConnector.addUserRole("id", institution, null, "MANAGER", List.of("admin"))).thenReturn("userId");
        when(userMsConnector.createUser(institution, null,"MANAGER", List.of("admin","operator"), userToOnboard2, true)).thenReturn("userId");

        List<RelationshipInfo> result = onboardingServiceImpl.onboardingUsers(request, "userName", "surname");

        verify(onboardingMsConnectorMock, times(1)).getInstitutionsByTaxCodeAndSubunitCode(anyString(), anyString());
        assert result.size() == 3;
    }

    @Test
    void onboardingUsers_noInstitutionFound() {
        OnboardingUsersRequest request = new OnboardingUsersRequest();
        request.setInstitutionTaxCode("taxCode");
        request.setInstitutionSubunitCode("subunitCode");
        request.setUsers(List.of(new UserToOnboard()));

        when(onboardingMsConnectorMock.getInstitutionsByTaxCodeAndSubunitCode(anyString(), anyString())).thenReturn(new ArrayList<>());

        assertThrows(ResourceNotFoundException.class, () -> onboardingServiceImpl.onboardingUsers(request, "userName", "surname"));
    }

    @Test
    void onboardingUsers_emptyUsersList() {
        OnboardingUsersRequest request = new OnboardingUsersRequest();
        request.setInstitutionTaxCode("taxCode");
        request.setInstitutionSubunitCode("subunitCode");
        request.setUsers(new ArrayList<>());

        Institution institution = new Institution();
        institution.setId("institutionId");

        when(onboardingMsConnectorMock.getInstitutionsByTaxCodeAndSubunitCode(anyString(), anyString())).thenReturn(List.of(institution));

        List<RelationshipInfo> result = onboardingServiceImpl.onboardingUsers(request, "userName", "surname");

        verify(onboardingMsConnectorMock, times(1)).getInstitutionsByTaxCodeAndSubunitCode(anyString(), anyString());
        assert result.isEmpty();
    }

}
