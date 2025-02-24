package it.pagopa.selfcare.external_api.utils;

import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionInfo;
import lombok.experimental.UtilityClass;

import java.util.function.BinaryOperator;

@UtilityClass
public class Utils {
    public static BinaryOperator<OnboardedInstitutionInfo> latestInstitutionByCreationDate() {
        return (existing, replacement) -> existing.getProductInfo().getCreatedAt()
                .isAfter(replacement.getProductInfo().getCreatedAt()) ? existing : replacement;
    }
}
