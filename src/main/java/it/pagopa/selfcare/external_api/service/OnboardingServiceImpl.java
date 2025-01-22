package it.pagopa.selfcare.external_api.service;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.commons.base.utils.ProductId;
import it.pagopa.selfcare.external_api.client.MsCoreInstitutionApiClient;
import it.pagopa.selfcare.external_api.client.MsOnboardingControllerApi;
import it.pagopa.selfcare.external_api.client.MsPartyRegistryProxyRestClient;
import it.pagopa.selfcare.external_api.client.MsUserApiRestClient;
import it.pagopa.selfcare.external_api.exception.ResourceNotFoundException;
import it.pagopa.selfcare.external_api.mapper.OnboardingMapper;
import it.pagopa.selfcare.external_api.mapper.UserResourceMapper;
import it.pagopa.selfcare.external_api.model.institution.Institution;
import it.pagopa.selfcare.external_api.model.onboarding.ImportContractDto;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingAggregatorImportData;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingAggregatorImportDto;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingData;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingImportContract;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingUsersRequest;
import it.pagopa.selfcare.external_api.model.user.OnboardedProduct;
import it.pagopa.selfcare.external_api.model.user.RelationshipInfo;
import it.pagopa.selfcare.external_api.model.user.RelationshipState;
import it.pagopa.selfcare.external_api.model.user.UserToOnboard;
import it.pagopa.selfcare.onboarding.common.InstitutionType;
import it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.AggregateInstitutionRequest;
import it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.InstitutionBaseRequest;
import it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.OnboardingAggregationImportRequest;
import it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.OnboardingResponse;
import it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.Origin;
import it.pagopa.selfcare.registry_proxy.generated.openapi.v1.dto.InstitutionResource;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.AddUserRoleDto;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.CreateUserDto;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.Product1;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
class OnboardingServiceImpl implements OnboardingService {

  private final MsOnboardingControllerApi onboardingControllerApi;
  private final OnboardingMapper onboardingMapper;
  private final MsCoreInstitutionApiClient institutionApiClient;
  private final MsUserApiRestClient msUserApiRestClient;
  private final UserResourceMapper userResourceMapper;
  private final MsPartyRegistryProxyRestClient msPartyRegistryProxyRestClient;

  @Override
  public void oldContractOnboardingV2(OnboardingData onboardingImportData) {
    log.trace("oldContractOnboarding start");
    log.debug("oldContractOnboarding = {}", onboardingImportData);
    onboardingControllerApi._onboardingPaImport(
        onboardingMapper.mapToOnboardingImportRequest(onboardingImportData));
    log.trace("oldContractOnboarding end");
  }

  @Override
  public void autoApprovalOnboardingProductV2(OnboardingData onboardingData) {
    log.trace("autoApprovalOnboarding start");
    log.debug("autoApprovalOnboarding = {}", onboardingData);
    if (onboardingData.getInstitutionType() == InstitutionType.PA) {
      onboardingControllerApi._onboardingPaCompletion(
          onboardingMapper.toOnboardingPaRequest(onboardingData));
    } else if (onboardingData.getInstitutionType() == InstitutionType.PSP) {
      onboardingControllerApi._onboardingPspCompletion(
          onboardingMapper.toOnboardingPspRequest(onboardingData));
    } else {
      onboardingControllerApi._onboardingCompletion(
          onboardingMapper.toOnboardingDefaultRequest(onboardingData));
    }
    log.trace("autoApprovalOnboarding end");
  }

  @Override
  public void autoApprovalOnboardingImportProductV2(OnboardingData onboardingData) {
    log.trace("autoApprovalOnboardingImport start");
    log.debug("autoApprovalOnboardingImport = {}", onboardingData);
    onboardingControllerApi._onboardingPspImport(
        onboardingMapper.toOnboardingImportPspRequest(onboardingData));
    log.trace("autoApprovalOnboardingImport end");
  }

  @Override
  public List<RelationshipInfo> onboardingUsers(
      OnboardingUsersRequest request, String userName, String surname) {
    Institution institution =
        Objects.requireNonNull(
                institutionApiClient
                    ._getInstitutionsUsingGET(
                        request.getInstitutionTaxCode(),
                        request.getInstitutionSubunitCode(),
                        null,
                        null,
                        null
                    )
                    .getBody())
            .getInstitutions()
            .stream()
            .map(onboardingMapper::toInstitution)
            .findFirst()
            .orElseThrow(
                () -> new ResourceNotFoundException("Institution not found for given value"));

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

  @Override
  public void aggregationImport(OnboardingAggregationImportRequest onboardingRequest) {
    log.trace("aggregationImport start");
    log.debug("aggregationImport = {}", onboardingRequest);
    onboardingControllerApi._onboardingAggregationImport(onboardingRequest).getBody();
    log.trace("aggregationImport end");
  }

  private List<RelationshipInfo> processUsersWithId(
      Map<String, List<UserToOnboard>> usersWithId, Institution institution, String productId) {
    List<RelationshipInfo> result = new ArrayList<>();

    usersWithId.forEach(
        (userId, users) -> {
          Map<PartyRole, List<UserToOnboard>> listOfRole =
              users.stream().collect(Collectors.groupingBy(UserToOnboard::getRole));
          listOfRole.forEach(
              (role, usersByRole) -> {
                List<String> productRoles =
                    users.stream().map(UserToOnboard::getProductRole).toList();
                users.stream()
                    .map(
                        userToOnboard ->
                            addUserRole(userId, institution, productId, role.name(), productRoles))
                    .forEach(
                        id ->
                            result.addAll(
                                buildRelationShipInfo(id, institution, productId, usersByRole)));
              });
        });

    return result;
  }

  private String addUserRole(
      String userId,
      Institution institution,
      String productId,
      String role,
      List<String> productRoles) {
    it.pagopa.selfcare.user.generated.openapi.v1.dto.Product product =
        it.pagopa.selfcare.user.generated.openapi.v1.dto.Product.builder()
            .productId(productId)
            .role(role)
            .productRoles(productRoles)
            .build();

    AddUserRoleDto addUserRoleDto =
        AddUserRoleDto.builder()
            .product(product)
            .institutionId(institution.getId())
            .institutionDescription(institution.getDescription())
            .institutionRootName(institution.getParentDescription())
            .build();

    msUserApiRestClient._createOrUpdateByUserId(userId, addUserRoleDto);

    return userId;
  }

  private List<RelationshipInfo> processUsersWithoutId(
      Map<String, List<UserToOnboard>> usersWithoutId, Institution institution, String productId) {
    List<RelationshipInfo> result = new ArrayList<>();

    usersWithoutId.forEach(
        (taxCode, users) -> {
          Map<PartyRole, List<UserToOnboard>> listOfRole =
              users.stream().collect(Collectors.groupingBy(UserToOnboard::getRole));
          listOfRole.forEach(
              (role, usersByRole) -> {
                List<String> productRoles =
                    users.stream().map(UserToOnboard::getProductRole).toList();
                String userId =
                    createUser(
                        institution,
                        productId,
                        role.name(),
                        productRoles,
                        usersByRole.get(0),
                        true);
                result.addAll(buildRelationShipInfo(userId, institution, productId, usersByRole));
              });
        });

    return result;
  }

  private String createUser(
      Institution institution,
      String productId,
      String role,
      List<String> productRoles,
      UserToOnboard user,
      boolean sendMail) {

    Product1 product =
        Product1.builder().productId(productId).role(role).productRoles(productRoles).build();

    CreateUserDto createUserDto =
        CreateUserDto.builder()
            .institutionId(institution.getId())
            .user(userResourceMapper.toUser(user))
            .product(product)
            .hasToSendEmail(sendMail)
            .institutionDescription(institution.getDescription())
            .institutionRootName(institution.getParentDescription())
            .build();

    String userId = msUserApiRestClient._createOrUpdateByFiscalCode(createUserDto).getBody();
    log.info("User created with id: {}", userId);
    return userId;
  }

  private List<RelationshipInfo> buildRelationShipInfo(
      String id, Institution institution, String productId, List<UserToOnboard> usersByRole) {
    return usersByRole.stream()
        .map(
            userToOnboard -> {
              RelationshipInfo relationshipInfo = new RelationshipInfo();
              relationshipInfo.setInstitution(institution);
              relationshipInfo.setOnboardedProduct(buildOnboardedProduct(userToOnboard, productId));
              relationshipInfo.setUserId(id);
              return relationshipInfo;
            })
        .toList();
  }

  private OnboardedProduct buildOnboardedProduct(UserToOnboard userToOnboard, String productId) {
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

  @Override
  public OnboardingAggregatorImportData onboardingAggregatorImportBuildRequest(
      OnboardingAggregatorImportDto request, String taxCodeRequest) {

    OnboardingAggregatorImportData onboardingData =
        onboardingMapper.mapToOnboardingAggregatorImportRequest(request);
    List<AggregateInstitutionRequest> aggregates = onboardingData.getAggregates();

    request
        .getAggregates()
        .forEach(
            taxCodeDto -> {
              InstitutionResource response =
                  msPartyRegistryProxyRestClient
                      ._findInstitutionUsingGET(taxCodeDto.getTaxCode(), null, null)
                      .getBody();
              assert response != null;
              aggregates.stream()
                  .filter(
                      aggregate -> aggregate.getTaxCode().equalsIgnoreCase(response.getTaxCode()))
                  .forEach(
                      aggregate -> {
                        aggregate.setDescription(response.getDescription());
                        aggregate.setDigitalAddress(response.getDigitalAddress());
                      });
            });

    InstitutionResource response =
        msPartyRegistryProxyRestClient
            ._findInstitutionUsingGET(taxCodeRequest, null, null)
            .getBody();
    assert response != null;
    onboardingData.setInstitution(
        InstitutionBaseRequest.builder()
            .taxCode(taxCodeRequest)
            .description(response.getDescription())
            .digitalAddress(response.getDigitalAddress())
            .build());

    buildContractPathIO(request.getImportContract(), onboardingData.getOnboardingImportContract());
    onboardingData.setIsAggregator(Boolean.TRUE);
    onboardingData.setProductId(ProductId.PROD_IO.getValue());
    InstitutionBaseRequest institution = onboardingData.getInstitution();
    institution
        .setInstitutionType(
            it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.InstitutionType.valueOf(
                request.getInstitutionType()));
    institution.setOrigin(Origin.IPA);
    institution.setImported(Boolean.TRUE);
    return onboardingData;
  }

  private void buildContractPathIO(@NotNull @Valid ImportContractDto importContract, @Valid OnboardingImportContract onboardingImportContract) {
    onboardingImportContract.setFilePath("parties/docs/io/" + importContract.getFilePath() + importContract.getFileName());
  }
}
