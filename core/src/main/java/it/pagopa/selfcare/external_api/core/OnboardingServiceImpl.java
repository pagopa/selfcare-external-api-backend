package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.external_api.api.MsPartyRegistryProxyConnector;
import it.pagopa.selfcare.external_api.api.PartyConnector;
import it.pagopa.selfcare.external_api.api.ProductsConnector;
import it.pagopa.selfcare.external_api.api.UserRegistryConnector;
import it.pagopa.selfcare.external_api.core.exception.OnboardingNotAllowedException;
import it.pagopa.selfcare.external_api.core.exception.UpdateNotAllowedException;
import it.pagopa.selfcare.external_api.core.strategy.OnboardingValidationStrategy;
import it.pagopa.selfcare.external_api.exceptions.InstitutionAlreadyOnboardedException;
import it.pagopa.selfcare.external_api.exceptions.InstitutionDoesNotExistException;
import it.pagopa.selfcare.external_api.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.onboarding.InstitutionType;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingData;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingImportData;
import it.pagopa.selfcare.external_api.model.product.Product;
import it.pagopa.selfcare.external_api.model.product.ProductRoleInfo;
import it.pagopa.selfcare.external_api.model.product.ProductStatus;
import it.pagopa.selfcare.external_api.model.user.*;
import it.pagopa.selfcare.external_api.model.user.mapper.CertifiedFieldMapper;
import it.pagopa.selfcare.external_api.model.user.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.validation.ValidationException;
import java.util.*;

@Service
@Slf4j
class OnboardingServiceImpl implements OnboardingService {

    protected static final String REQUIRED_INSTITUTION_TYPE_MESSAGE = "An institution type is required";
    protected static final String REQUIRED_ONBOARDING_DATA_MESSAGE = "Onboarding data is required";
    protected static final String REQUIRED_INSTITUTION_BILLING_DATA_MESSAGE = "Institution's billing data are required";
    private static final String ONBOARDING_NOT_ALLOWED_ERROR_MESSAGE_TEMPLATE = "Institution with external id '%s' is not allowed to onboard '%s' product";
    protected static final String ATLEAST_ONE_PRODUCT_ROLE_REQUIRED = "At least one Product role related to %s Party role is required";
    protected static final String MORE_THAN_ONE_PRODUCT_ROLE_AVAILABLE = "More than one Product role related to %s Party role is available. Cannot automatically set the Product role";

    private static final EnumSet<User.Fields> USER_FIELD_LIST = EnumSet.of(User.Fields.name, User.Fields.familyName, User.Fields.workContacts);

    private final PartyConnector partyConnector;
    private final MsPartyRegistryProxyConnector registryProxyConnector;
    private final ProductsConnector productsConnector;
    private final UserRegistryConnector userConnector;
    private final OnboardingValidationStrategy onboardingValidationStrategy;

    @Autowired
    OnboardingServiceImpl(PartyConnector partyConnector,
                          ProductsConnector productsConnector,
                          UserRegistryConnector userConnector,
                          OnboardingValidationStrategy onboardingValidationStrategy,
                          MsPartyRegistryProxyConnector registryProxyConnector) {
        this.partyConnector = partyConnector;
        this.productsConnector = productsConnector;
        this.userConnector = userConnector;
        this.onboardingValidationStrategy = onboardingValidationStrategy;
        this.registryProxyConnector = registryProxyConnector;
    }

    @Override
    public void oldContractOnboarding(OnboardingImportData onboardingImportData) {
        log.trace("oldContractOnboarding start");
        log.debug("oldContractOnboarding = {}", onboardingImportData);
        Assert.notNull(onboardingImportData, REQUIRED_ONBOARDING_DATA_MESSAGE);

        try {
            ResponseEntity<Void> responseEntity = partyConnector.verifyOnboarding(onboardingImportData.getInstitutionExternalId(), onboardingImportData.getProductId());
            if (responseEntity.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
                log.debug((String.format("oldContractOnboarding: the institution with external id %s is already onboarded on the product %s",
                        onboardingImportData.getInstitutionExternalId(),
                        onboardingImportData.getProductId())));
                throw new InstitutionAlreadyOnboardedException(String.format("The institution with external id %s is already onboarded on the product %s",
                        onboardingImportData.getInstitutionExternalId(),
                        onboardingImportData.getProductId()));
            }
        } catch (ResourceNotFoundException | InstitutionDoesNotExistException exception) {
            log.debug(String.format("oldContractOnboarding: starting onboarding process for institution %s on product %s",
                    onboardingImportData.getInstitutionExternalId(),
                    onboardingImportData.getProductId()));

            Institution institution = null;
            try {
                institution = partyConnector.getInstitutionByExternalId(onboardingImportData.getInstitutionExternalId());
                if (institution.getInstitutionType() == null) {
                    setInstitutionType(onboardingImportData);
                } else {
                    onboardingImportData.setInstitutionType(institution.getInstitutionType());
                }
            } catch (ResourceNotFoundException e) {
                setInstitutionType(onboardingImportData);
            }

            Product product = productsConnector.getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType());
            Assert.notNull(product, "Product is required");

            if (product.getStatus() == ProductStatus.PHASE_OUT) {
                throw new ValidationException(String.format("Unable to complete the onboarding for institution with external id '%s' to product '%s', the product is dismissed.",
                        onboardingImportData.getInstitutionExternalId(),
                        product.getId()));
            }

            onboardingImportData.setContractPath(product.getContractTemplatePath());
            onboardingImportData.setContractVersion(product.getContractTemplateVersion());

            final EnumMap<PartyRole, ProductRoleInfo> roleMappings = getRoleMappings(product, onboardingImportData.getInstitutionExternalId());

            onboardingImportData.setProductName(product.getTitle());
            Assert.notNull(roleMappings, "Role mappings is required");
            onboardingImportData.getUsers().forEach(userInfo -> {
                Assert.notNull(roleMappings.get(userInfo.getRole()),
                        String.format(ATLEAST_ONE_PRODUCT_ROLE_REQUIRED, userInfo.getRole()));
                Assert.notEmpty(roleMappings.get(userInfo.getRole()).getRoles(),
                        String.format(ATLEAST_ONE_PRODUCT_ROLE_REQUIRED, userInfo.getRole()));
                Assert.state(roleMappings.get(userInfo.getRole()).getRoles().size() == 1,
                        String.format(MORE_THAN_ONE_PRODUCT_ROLE_AVAILABLE, userInfo.getRole()));
                userInfo.setProductRole(roleMappings.get(userInfo.getRole()).getRoles().get(0).getCode());
            });

            if (institution == null) {
                institution = partyConnector.createInstitutionUsingExternalId(onboardingImportData.getInstitutionExternalId());
            }

            String finalInstitutionInternalId = institution.getId();
            onboardingImportData.getUsers().forEach(user -> {

                final Optional<User> searchResult =
                        userConnector.search(user.getTaxCode(), USER_FIELD_LIST);
                searchResult.ifPresentOrElse(foundUser -> {
                    Optional<MutableUserFieldsDto> updateRequest = createUpdateRequest(user, foundUser, finalInstitutionInternalId);
                    updateRequest.ifPresent(mutableUserFieldsDto ->
                            userConnector.updateUser(UUID.fromString(foundUser.getId()), mutableUserFieldsDto));
                    user.setId(foundUser.getId());
                }, () -> user.setId(userConnector.saveUser(UserMapper.toSaveUserDto(user, finalInstitutionInternalId))
                        .getId().toString()));
            });

            setOnboardingImportDataFields(onboardingImportData, institution);

            partyConnector.oldContractOnboardingOrganization(onboardingImportData);
            log.trace("oldContractOnboarding end");
        }
    }

    @Override
    public void autoApprovalOnboarding(OnboardingData onboardingData) {
        log.trace("autoApprovalOnboarding start");
        log.debug("autoApprovalOnboarding = {}", onboardingData);
        Assert.notNull(onboardingData, REQUIRED_ONBOARDING_DATA_MESSAGE);
        Assert.notNull(onboardingData.getBilling(), REQUIRED_INSTITUTION_BILLING_DATA_MESSAGE);
        Assert.notNull(onboardingData.getInstitutionType(), REQUIRED_INSTITUTION_TYPE_MESSAGE);

        try {
            ResponseEntity<Void> responseEntity = partyConnector.verifyOnboarding(onboardingData.getInstitutionExternalId(), onboardingData.getProductId());
            if (responseEntity.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
                log.debug((String.format("autoApprovalOnboarding: the institution with external id %s is already onboarded on the product %s",
                        onboardingData.getInstitutionExternalId(),
                        onboardingData.getProductId())));
                throw new InstitutionAlreadyOnboardedException(String.format("The institution with external id %s is already onboarded on the product %s",
                        onboardingData.getInstitutionExternalId(),
                        onboardingData.getProductId()));
            }
        } catch (ResourceNotFoundException | InstitutionDoesNotExistException exception) {
            log.debug(String.format("autoApprovalOnboarding: starting onboarding process for institution %s on product %s",
                    onboardingData.getInstitutionExternalId(),
                    onboardingData.getProductId()));

            Product product = productsConnector.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
            Assert.notNull(product, "Product is required");

            if (product.getStatus() == ProductStatus.PHASE_OUT) {
                throw new ValidationException(String.format("Unable to complete the onboarding for institution with external id '%s' to product '%s', the product is dismissed.",
                        onboardingData.getInstitutionExternalId(),
                        product.getId()));
            }

            onboardingData.setContractPath(product.getContractTemplatePath());
            onboardingData.setContractVersion(product.getContractTemplateVersion());

            final EnumMap<PartyRole, ProductRoleInfo> roleMappings = getRoleMappings(product, onboardingData.getInstitutionExternalId());

            onboardingData.setProductName(product.getTitle());
            Assert.notNull(roleMappings, "Role mappings is required");
            onboardingData.getUsers().forEach(userInfo -> {
                Assert.notNull(roleMappings.get(userInfo.getRole()),
                        String.format(ATLEAST_ONE_PRODUCT_ROLE_REQUIRED, userInfo.getRole()));
                Assert.notEmpty(roleMappings.get(userInfo.getRole()).getRoles(),
                        String.format(ATLEAST_ONE_PRODUCT_ROLE_REQUIRED, userInfo.getRole()));
                Assert.state(roleMappings.get(userInfo.getRole()).getRoles().size() == 1,
                        String.format(MORE_THAN_ONE_PRODUCT_ROLE_AVAILABLE, userInfo.getRole()));
                userInfo.setProductRole(roleMappings.get(userInfo.getRole()).getRoles().get(0).getCode());
            });

            Institution institution;
            try {
                institution = partyConnector.getInstitutionByExternalId(onboardingData.getInstitutionExternalId());
            } catch (ResourceNotFoundException e) {
                if (InstitutionType.PA.equals(onboardingData.getInstitutionType()) ||
                        (InstitutionType.GSP.equals(onboardingData.getInstitutionType()) && onboardingData.getProductId().equals("prod-interop")
                                && onboardingData.getOrigin().equals("IPA"))) {
                    institution = partyConnector.createInstitutionUsingExternalId(onboardingData.getInstitutionExternalId());
                } else {
                    institution = partyConnector.createInstitutionRaw(onboardingData);
                }
            }
            String finalInstitutionInternalId = institution.getId();
            onboardingData.getUsers().forEach(user -> {

                final Optional<User> searchResult =
                        userConnector.search(user.getTaxCode(), USER_FIELD_LIST);
                searchResult.ifPresentOrElse(foundUser -> {
                    Optional<MutableUserFieldsDto> updateRequest = createUpdateRequest(user, foundUser, finalInstitutionInternalId);
                    updateRequest.ifPresent(mutableUserFieldsDto ->
                            userConnector.updateUser(UUID.fromString(foundUser.getId()), mutableUserFieldsDto));
                    user.setId(foundUser.getId());
                }, () -> user.setId(userConnector.saveUser(UserMapper.toSaveUserDto(user, finalInstitutionInternalId))
                        .getId().toString()));
            });

            partyConnector.autoApprovalOnboarding(onboardingData);
            log.trace("autoApprovalOnboarding end");
        }
    }

    @Override
    public ResponseEntity<Void> verifyOnboarding(String externalInstitutionId, String productId) {
        log.trace("verifyOnboarding start");
        log.debug("verifyOnboarding externalInstitutionId = {}", externalInstitutionId);
        ResponseEntity<Void> responseEntity = partyConnector.verifyOnboarding(externalInstitutionId, productId);
        log.trace("verifyOnboarding end");
        return responseEntity;
    }

    private Optional<MutableUserFieldsDto> createUpdateRequest(it.pagopa.selfcare.external_api.model.onboarding.User user, User foundUser, String institutionInternalId) {
        Optional<MutableUserFieldsDto> mutableUserFieldsDto = Optional.empty();
        if (isFieldToUpdate(foundUser.getName(), user.getName())) {
            MutableUserFieldsDto dto = new MutableUserFieldsDto();
            dto.setName(CertifiedFieldMapper.map(user.getName()));
            mutableUserFieldsDto = Optional.of(dto);
        }
        if (isFieldToUpdate(foundUser.getFamilyName(), user.getSurname())) {
            MutableUserFieldsDto dto = mutableUserFieldsDto.orElseGet(MutableUserFieldsDto::new);
            dto.setFamilyName(CertifiedFieldMapper.map(user.getSurname()));
            mutableUserFieldsDto = Optional.of(dto);
        }
        if (foundUser.getWorkContacts() == null
                || !foundUser.getWorkContacts().containsKey(institutionInternalId)
                || isFieldToUpdate(foundUser.getWorkContacts().get(institutionInternalId).getEmail(), user.getEmail())) {
            MutableUserFieldsDto dto = mutableUserFieldsDto.orElseGet(MutableUserFieldsDto::new);
            final WorkContact workContact = new WorkContact();
            workContact.setEmail(CertifiedFieldMapper.map(user.getEmail()));
            dto.setWorkContacts(Map.of(institutionInternalId, workContact));
            mutableUserFieldsDto = Optional.of(dto);
        }
        return mutableUserFieldsDto;
    }

    private boolean isFieldToUpdate(CertifiedField<String> certifiedField, String value) {
        boolean isToUpdate = true;
        if (certifiedField != null) {
            if (Certification.NONE.equals(certifiedField.getCertification())) {
                if (certifiedField.getValue().equals(value)) {
                    isToUpdate = false;
                }
            } else {
                if (certifiedField.getValue().equalsIgnoreCase(value)) {
                    isToUpdate = false;
                } else {
                    throw new UpdateNotAllowedException(String.format("Update user request not allowed because of value %s", value));
                }
            }
        }
        return isToUpdate;
    }

    private void validateOnboarding(String externalInstitutionId, String productId) {
        if (!onboardingValidationStrategy.validate(productId, externalInstitutionId)) {
            throw new OnboardingNotAllowedException(String.format(ONBOARDING_NOT_ALLOWED_ERROR_MESSAGE_TEMPLATE,
                    externalInstitutionId,
                    productId));
        }
    }

    private void setOnboardingImportDataFields(OnboardingImportData onboardingImportData, Institution institution) {
        onboardingImportData.getBilling().setVatNumber(institution.getTaxCode());
        onboardingImportData.getBilling().setRecipientCode(institution.getOriginId());
        onboardingImportData.getBilling().setPublicServices(true);
        onboardingImportData.getInstitutionUpdate().setDescription(institution.getDescription());
        onboardingImportData.getInstitutionUpdate().setDigitalAddress(institution.getDigitalAddress());
        onboardingImportData.getInstitutionUpdate().setAddress(institution.getAddress());
        onboardingImportData.getInstitutionUpdate().setTaxCode(institution.getTaxCode());
        onboardingImportData.getInstitutionUpdate().setZipCode(institution.getZipCode());
        onboardingImportData.getInstitutionUpdate().setGeographicTaxonomies(Collections.emptyList());
        onboardingImportData.setOrigin(institution.getOrigin());
    }

    private void setInstitutionType(OnboardingImportData onboardingImportData) {
        String institutionCategory = registryProxyConnector.getInstitutionCategory(onboardingImportData.getInstitutionExternalId());
        if (institutionCategory.equals("L37")) {
            onboardingImportData.setInstitutionType(InstitutionType.GSP);
        } else {
            onboardingImportData.setInstitutionType(InstitutionType.PA);
        }
    }

    private EnumMap<PartyRole, ProductRoleInfo> getRoleMappings(Product product, String institutionExternalId) {
        if (product.getParentId() != null) {
            final Product baseProduct = productsConnector.getProduct(product.getParentId(), null);
            verifyBaseProductOnboarding(baseProduct, product, institutionExternalId);
            return baseProduct.getRoleMappings();
        } else {
            validateOnboarding(institutionExternalId, product.getId());
            return product.getRoleMappings();
        }
    }

    private void verifyBaseProductOnboarding(Product baseProduct, Product product, String institutionExternalId) {
        if (baseProduct.getStatus() == ProductStatus.PHASE_OUT) {
            throw new ValidationException(String.format("Unable to complete the onboarding for institution with external id '%s' to product '%s', the base product is dismissed.",
                    institutionExternalId,
                    baseProduct.getId()));
        }
        validateOnboarding(institutionExternalId, baseProduct.getId());
        try {
            partyConnector.verifyOnboarding(institutionExternalId, baseProduct.getId());
        } catch (RuntimeException e) {
            throw new ValidationException(String.format("Unable to complete the onboarding for institution with external id '%s' to product '%s'. Please onboard first the '%s' product for the same institution",
                    institutionExternalId,
                    product.getId(),
                    baseProduct.getId()));
        }
    }

}
