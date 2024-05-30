package it.pagopa.selfcare.external_api.connector.rest.mapper;

import it.pagopa.selfcare.external_api.model.nationalRegistries.LegalVerification;
import it.pagopa.selfcare.registry_proxy.generated.openapi.v1.dto.LegalVerificationResult;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RegistryProxyMapper {

    LegalVerification toLegalVerification(LegalVerificationResult model);
}
