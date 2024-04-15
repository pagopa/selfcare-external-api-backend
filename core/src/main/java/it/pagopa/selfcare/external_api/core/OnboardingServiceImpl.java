package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.external_api.api.OnboardingMsConnector;
import it.pagopa.selfcare.external_api.api.UserMsConnector;
import it.pagopa.selfcare.external_api.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingData;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingUsersRequest;
import it.pagopa.selfcare.external_api.model.user.OnboardedProduct;
import it.pagopa.selfcare.external_api.model.user.RelationshipInfo;
import it.pagopa.selfcare.external_api.model.user.RelationshipState;
import it.pagopa.selfcare.external_api.model.user.UserToOnboard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
class OnboardingServiceImpl implements OnboardingService {

    private final OnboardingMsConnector onboardingMsConnector;
    private final UserMsConnector userMsConnector;

    @Override
    public void oldContractOnboardingV2(OnboardingData onboardingImportData) {
        log.trace("oldContractOnboarding start");
        log.debug("oldContractOnboarding = {}", onboardingImportData);
        onboardingMsConnector.onboardingImportPA(onboardingImportData);
        log.trace("oldContractOnboarding end");
    }

    @Override
    public void autoApprovalOnboardingProductV2(OnboardingData onboardingData) {
        log.trace("autoApprovalOnboarding start");
        log.debug("autoApprovalOnboarding = {}", onboardingData);
        onboardingMsConnector.onboarding(onboardingData);
        log.trace("autoApprovalOnboarding end");
    }

    @Override
    public List<RelationshipInfo> onboardingUsers(OnboardingUsersRequest request, String userName, String surname) {
        Institution institution = onboardingMsConnector.getInstitutionsByTaxCodeAndSubunitCode(request.getInstitutionTaxCode(), request.getInstitutionSubunitCode()).stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found for given value"));

        Map<String, List<UserToOnboard>> usersWithId = new HashMap<>();
        Map<String, List<UserToOnboard>> usersWithoutId = new HashMap<>();

        for (UserToOnboard user : request.getUsers()) {
            if (StringUtils.hasText(user.getId())) {
                usersWithId.computeIfAbsent(user.getId(), k -> new ArrayList<>()).add(user);
            } else {
                usersWithoutId.computeIfAbsent(user.getTaxCode(), k -> new ArrayList<>()).add(user);
            }
        }

        List<RelationshipInfo> result = new ArrayList<>();
        result.addAll(processUsersWithId(usersWithId, institution, request.getProductId()));
        result.addAll(processUsersWithoutId(usersWithoutId, institution, request.getProductId()));

        return result;
    }

    private List<RelationshipInfo> processUsersWithId(Map<String, List<UserToOnboard>> usersWithId, Institution institution, String productId) {
        List<RelationshipInfo> result = new ArrayList<>();

        usersWithId.forEach((userId, users) -> {
            Map<PartyRole, List<UserToOnboard>> listOfRole = users.stream().collect(Collectors.groupingBy(UserToOnboard::getRole));
            listOfRole.forEach((role, usersByRole) -> {
                List<String> productRoles = users.stream().map(UserToOnboard::getProductRole).collect(Collectors.toList());
                users.stream().map(userToOnboard -> userMsConnector.addUserRole(userId, institution, productId, role.name(), productRoles))
                        .forEach(id -> result.addAll(buildRelationShipInfo(id, institution, productId, usersByRole)));
            });
        });

        return result;
    }

    private List<RelationshipInfo> processUsersWithoutId(Map<String, List<UserToOnboard>> usersWithoutId, Institution institution, String productId) {
        List<RelationshipInfo> result = new ArrayList<>();

        usersWithoutId.forEach((taxCode, users) -> {
            Map<PartyRole, List<UserToOnboard>> listOfRole = users.stream().collect(Collectors.groupingBy(UserToOnboard::getRole));
            listOfRole.forEach((role, usersByRole) -> {
                List<String> productRoles = users.stream().map(UserToOnboard::getProductRole).collect(Collectors.toList());
                String userId = userMsConnector.createUser(institution, productId, role.name(), productRoles, usersByRole.get(0));
                result.addAll(buildRelationShipInfo(userId, institution, productId, usersByRole));
            });
        });

        return result;
    }

    private List<RelationshipInfo> buildRelationShipInfo(String id, Institution institution, String productId, List<UserToOnboard> usersByRole) {
        return usersByRole.stream()
                .map(userToOnboard -> {
                    RelationshipInfo relationshipInfo = new RelationshipInfo();
                    relationshipInfo.setInstitution(institution);
                    relationshipInfo.setOnboardedProduct(buildOnboardedProduct(userToOnboard, productId));
                    relationshipInfo.setUserId(id);
                    return relationshipInfo;
                })
                .toList();
    }

    private OnboardedProduct buildOnboardedProduct(UserToOnboard userToOnboard, String productId){
        OnboardedProduct onboardedProduct = new OnboardedProduct();
        onboardedProduct.setProductId(productId);
        onboardedProduct.setRole(userToOnboard.getRole());
        onboardedProduct.setProductRole(userToOnboard.getProductRole());
        onboardedProduct.setStatus(RelationshipState.ACTIVE);
        onboardedProduct.setEnv(userToOnboard.getEnv());
        onboardedProduct.setCreatedAt(OffsetDateTime.now());
        onboardedProduct.setUpdatedAt(OffsetDateTime.now());
        return onboardedProduct;
    }
}