package it.pagopa.selfcare.external_api.web.model.mapper;

import it.pagopa.selfcare.external_api.model.nationalRegistries.LegalVerification;
import it.pagopa.selfcare.external_api.web.model.national_registries.LegalVerificationResource;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NationalRegistryMapper {
    LegalVerificationResource toResource(LegalVerification model);
}
