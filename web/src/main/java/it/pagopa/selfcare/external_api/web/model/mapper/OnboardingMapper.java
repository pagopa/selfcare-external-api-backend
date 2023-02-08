package it.pagopa.selfcare.external_api.web.model.mapper;


import it.pagopa.selfcare.external_api.model.onboarding.*;
import it.pagopa.selfcare.external_api.web.model.onboarding.ImportContractDto;
import it.pagopa.selfcare.external_api.web.model.onboarding.OnboardingImportDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OnboardingMapper {

    public static ImportContract fromDto(ImportContractDto model) {
        ImportContract resource = null;
        if (model != null) {
            resource = new ImportContract();
            resource.setFileName(model.getFileName());
            resource.setFilePath(model.getFilePath());
            resource.setContractType(model.getContractType());
        }
        return resource;
    }

    public static OnboardingImportData toOnboardingImportData(String externalId, OnboardingImportDto model) {
        OnboardingImportData resource = null;
        if (model != null) {
            resource = new OnboardingImportData();
            resource.setInstitutionExternalId(externalId);
            resource.setProductId("prod-io");
            resource.setUsers(model.getUsers().stream()
                    .map(UserMapper::toUser)
                    .collect(Collectors.toList()));
            resource.setImportContract(fromDto(model.getImportContract()));
            resource.setBilling(new Billing());
            resource.setInstitutionUpdate(new InstitutionUpdate());
            resource.setInstitutionType(InstitutionType.PA);
        }
        return resource;
    }
}
