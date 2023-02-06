package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.external_api.api.PartyConnector;
import it.pagopa.selfcare.external_api.api.ProductsConnector;
import it.pagopa.selfcare.external_api.api.UserRegistryConnector;
import it.pagopa.selfcare.external_api.core.exception.OnboardingNotAllowedException;
import it.pagopa.selfcare.external_api.core.exception.UpdateNotAllowedException;
import it.pagopa.selfcare.external_api.core.strategy.OnboardingValidationStrategy;
import it.pagopa.selfcare.external_api.exception.ResourceNotFoundException;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingImportData;
import it.pagopa.selfcare.external_api.model.product.Product;
import it.pagopa.selfcare.external_api.model.product.ProductRoleInfo;
import it.pagopa.selfcare.external_api.model.product.ProductStatus;
import it.pagopa.selfcare.external_api.model.user.*;
import it.pagopa.selfcare.external_api.model.user.mapper.CertifiedFieldMapper;
import it.pagopa.selfcare.external_api.model.user.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.validation.ValidationException;
import java.util.*;

@Service
@Slf4j
class OnboardingServiceImpl implements OnboardingService {

    protected static final String REQUIRED_INSTITUTION_TYPE_MESSAGE = "An institution type is required";
    protected static final String REQUIRED_ONBOARDING_DATA_MESSAGE = "Onboarding data is required";
    private static final String ONBOARDING_NOT_ALLOWED_ERROR_MESSAGE_TEMPLATE = "Institution with external id '%s' is not allowed to onboard '%s' product";
    protected static final String ATLEAST_ONE_PRODUCT_ROLE_REQUIRED = "At least one Product role related to %s Party role is required";
    protected static final String MORE_THAN_ONE_PRODUCT_ROLE_AVAILABLE = "More than one Product role related to %s Party role is available. Cannot automatically set the Product role";

    private static final EnumSet<User.Fields> USER_FIELD_LIST = EnumSet.of(User.Fields.name, User.Fields.familyName, User.Fields.workContacts);

    private final PartyConnector partyConnector;
    private final ProductsConnector productsConnector;
    private final UserRegistryConnector userConnector;
    private final OnboardingValidationStrategy onboardingValidationStrategy;

    @Autowired
    OnboardingServiceImpl(PartyConnector partyConnector,
                          ProductsConnector productsConnector,
                          UserRegistryConnector userConnector,
                          OnboardingValidationStrategy onboardingValidationStrategy) {
        this.partyConnector = partyConnector;
        this.productsConnector = productsConnector;
        this.userConnector = userConnector;
        this.onboardingValidationStrategy = onboardingValidationStrategy;
    }

    @Override
    public void oldContractOnboarding(OnboardingImportData onboardingImportData) {
        log.trace("oldContractOnboarding start");
        log.debug("oldContractOnboarding = {}", onboardingImportData);
        Assert.notNull(onboardingImportData, REQUIRED_ONBOARDING_DATA_MESSAGE);
        Assert.notNull(onboardingImportData.getInstitutionType(), REQUIRED_INSTITUTION_TYPE_MESSAGE);

        Product product = productsConnector.getProduct(onboardingImportData.getProductId(), onboardingImportData.getInstitutionType());
        Assert.notNull(product, "Product is required");

        if (product.getStatus() == ProductStatus.PHASE_OUT) {
            throw new ValidationException(String.format("Unable to complete the onboarding for institution with external id '%s' to product '%s', the product is dismissed.",
                    onboardingImportData.getInstitutionExternalId(),
                    product.getId()));
        }

        onboardingImportData.setContractPath(product.getContractTemplatePath());
        onboardingImportData.setContractVersion(product.getContractTemplateVersion());

        final EnumMap<PartyRole, ProductRoleInfo> roleMappings;
        if (product.getParentId() != null) {
            final Product baseProduct = productsConnector.getProduct(product.getParentId(), null);
            if (baseProduct.getStatus() == ProductStatus.PHASE_OUT) {
                throw new ValidationException(String.format("Unable to complete the onboarding for institution with external id '%s' to product '%s', the base product is dismissed.",
                        onboardingImportData.getInstitutionExternalId(),
                        baseProduct.getId()));
            }
            validateOnboarding(onboardingImportData.getInstitutionExternalId(), baseProduct.getId());
            try {
                partyConnector.verifyOnboarding(onboardingImportData.getInstitutionExternalId(), baseProduct.getId());
            } catch (RuntimeException e) {
                throw new ValidationException(String.format("Unable to complete the onboarding for institution with external id '%s' to product '%s'. Please onboard first the '%s' product for the same institution",
                        onboardingImportData.getInstitutionExternalId(),
                        product.getId(),
                        baseProduct.getId()));
            }
            roleMappings = baseProduct.getRoleMappings();
        } else {
            validateOnboarding(onboardingImportData.getInstitutionExternalId(), product.getId());
            roleMappings = product.getRoleMappings();
        }

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

        Institution institution;
        try {
            institution = partyConnector.getInstitutionByExternalId(onboardingImportData.getInstitutionExternalId());
        } catch (ResourceNotFoundException e) {
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

        partyConnector.oldContractOnboardingOrganization(onboardingImportData);
        log.trace("oldContractOnboarding end");
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
}