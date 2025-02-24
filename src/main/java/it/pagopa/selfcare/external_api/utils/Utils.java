package it.pagopa.selfcare.external_api.utils;

import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionInfo;
import lombok.experimental.UtilityClass;

import java.util.function.BinaryOperator;

@UtilityClass
public class Utils {
    public static BinaryOperator<OnboardedInstitutionInfo> latestInstitutionByCreationDate() {
        return (existing, replacement) -> {
            if(existing == null && replacement == null) throw new NullPointerException();
            if(existing == null) return replacement;
            if(replacement == null) return existing;
            return existing.getProductInfo().getCreatedAt()
                    .isAfter(replacement.getProductInfo().getCreatedAt()) ? existing : replacement;
        };
    }
}
