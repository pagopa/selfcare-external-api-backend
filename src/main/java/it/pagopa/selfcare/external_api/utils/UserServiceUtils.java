package it.pagopa.selfcare.external_api.utils;

import it.pagopa.selfcare.external_api.model.onboarding.OnboardedInstitutionInfo;
import lombok.experimental.UtilityClass;

import java.util.Objects;
import java.util.function.BinaryOperator;

@UtilityClass
public class UserServiceUtils {
    public static BinaryOperator<OnboardedInstitutionInfo> latestInstitutionByCreationDate() {
        return (existing, replacement) -> {
            if(Objects.isNull(existing) && Objects.isNull(replacement)) throw new NullPointerException();
            if(Objects.isNull(existing)) return replacement;
            if(Objects.isNull(replacement)) return existing;
            return existing.getProductInfo().getCreatedAt()
                    .isAfter(replacement.getProductInfo().getCreatedAt()) ? existing : replacement;
        };
    }
}
