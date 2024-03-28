package it.pagopa.selfcare.external_api.core;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import it.pagopa.selfcare.external_api.api.*;
import it.pagopa.selfcare.external_api.core.exception.OnboardingNotAllowedException;
import it.pagopa.selfcare.external_api.core.exception.UpdateNotAllowedException;
import it.pagopa.selfcare.external_api.core.strategy.OnboardingValidationStrategy;
import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.onboarding.OnboardingData;
import it.pagopa.selfcare.external_api.model.product.Product;
import it.pagopa.selfcare.external_api.model.product.ProductRoleInfo;
import it.pagopa.selfcare.external_api.model.product.ProductStatus;
import it.pagopa.selfcare.external_api.model.user.*;
import it.pagopa.selfcare.external_api.model.user.mapper.CertifiedFieldMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.ValidationException;
import java.util.*;

import static it.pagopa.selfcare.commons.base.utils.Origin.*;
import static it.pagopa.selfcare.commons.base.utils.ProductId.PROD_INTEROP;


@Service
@Slf4j
class OnboardingServiceImpl implements OnboardingService {

    private static final String ONBOARDING_NOT_ALLOWED_ERROR_MESSAGE_TEMPLATE = "Institution with external id '%s' is not allowed to onboard '%s' product";

    private final PartyConnector partyConnector;
    private final ProductsConnector productsConnector;
    private final OnboardingValidationStrategy onboardingValidationStrategy;
    private final OnboardingMsConnector onboardingMsConnector;

    @Autowired
    OnboardingServiceImpl(PartyConnector partyConnector,
                          ProductsConnector productsConnector,
                          OnboardingValidationStrategy onboardingValidationStrategy,
                          OnboardingMsConnector onboardingMsConnector) {
        this.partyConnector = partyConnector;
        this.productsConnector = productsConnector;
        this.onboardingValidationStrategy = onboardingValidationStrategy;
        this.onboardingMsConnector = onboardingMsConnector;
    }

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
            }
            else {
                if (certifiedField.getValue().equalsIgnoreCase(value)) {
                    isToUpdate = false;
                }
                else {
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

    private EnumMap<PartyRole, ProductRoleInfo> getRoleMappings(Product product, String institutionExternalId) {
        if (product.getParentId() != null) {
            final Product baseProduct = productsConnector.getProduct(product.getParentId(), null);
            verifyBaseProductOnboarding(baseProduct, product, institutionExternalId);
            return baseProduct.getRoleMappings();
        }
        else {
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

    private Institution createInstitution(OnboardingData onboardingData) {
        Institution institution;
        if (InstitutionType.SA.equals(onboardingData.getInstitutionType()) && onboardingData.getOrigin().equalsIgnoreCase(ANAC.getValue())) {
            institution = partyConnector.createInstitutionFromANAC(onboardingData);
        }
        else if (InstitutionType.AS.equals(onboardingData.getInstitutionType()) && onboardingData.getOrigin().equalsIgnoreCase(IVASS.getValue())) {
            institution = partyConnector.createInstitutionFromIVASS(onboardingData);
        }
        else if (InstitutionType.PA.equals(onboardingData.getInstitutionType()) ||
                (InstitutionType.GSP.equals(onboardingData.getInstitutionType()) && onboardingData.getProductId().equals(PROD_INTEROP.getValue())
                        && onboardingData.getOrigin().equals(IPA.getValue()))) {
            institution = partyConnector.createInstitutionUsingExternalId(onboardingData.getInstitutionExternalId());
        }
        else {
            institution = partyConnector.createInstitutionRaw(onboardingData);
        }
        return institution;
    }

}