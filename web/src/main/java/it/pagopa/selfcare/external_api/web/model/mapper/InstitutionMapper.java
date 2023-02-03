package it.pagopa.selfcare.external_api.web.model.mapper;

import it.pagopa.selfcare.external_api.model.institutions.Institution;
import it.pagopa.selfcare.external_api.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.external_api.web.model.institutions.InstitutionDetailResource;
import it.pagopa.selfcare.external_api.web.model.institutions.InstitutionResource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InstitutionMapper {

    public static InstitutionResource toResource(InstitutionInfo model) {
        InstitutionResource resource = null;
        if (model != null) {
            resource = new InstitutionResource();
            if (model.getId() != null) {
                resource.setId(UUID.fromString(model.getId()));
            }
            resource.setDescription(model.getDescription());
            resource.setExternalId(model.getExternalId());
            resource.setAddress(model.getAddress());
            resource.setStatus(model.getStatus());
            resource.setDigitalAddress(model.getDigitalAddress());
            resource.setTaxCode(model.getTaxCode());
            resource.setZipCode(model.getZipCode());
            resource.setOrigin(model.getOrigin());
            resource.setOriginId(model.getOriginId());
            if (model.getProductRoles() != null)
                resource.setUserProductRoles(model.getProductRoles());
        }
        return resource;
    }

    public static InstitutionDetailResource toResource(Institution model) {
        InstitutionDetailResource resource = null;
        if (model != null) {
            resource = new InstitutionDetailResource();
            if (model.getId() != null) {
                resource.setId(UUID.fromString(model.getId()));
            }
            resource.setDescription(model.getDescription());
            resource.setExternalId(model.getExternalId());
            resource.setAddress(model.getAddress());
            resource.setDigitalAddress(model.getDigitalAddress());
            resource.setGeographicTaxonomies(model.getGeographicTaxonomies().stream()
                    .map(GeographicTaxonomyMapper::toResource)
                    .collect(Collectors.toList()));
            resource.setTaxCode(model.getTaxCode());
            resource.setZipCode(model.getZipCode());
            resource.setInstitutionType(model.getInstitutionType());
            resource.setOrigin(model.getOrigin());
            resource.setOriginId(model.getOriginId());
            resource.setRea(model.getRea());
            resource.setShareCapital(model.getShareCapital());
            resource.setBusinessRegisterPlace(model.getBusinessRegisterPlace());
            resource.setSupportEmail(model.getSupportEmail());
            resource.setSupportPhone(model.getSupportPhone());
            resource.setImported(model.getImported());
        }
        return resource;
    }
}
