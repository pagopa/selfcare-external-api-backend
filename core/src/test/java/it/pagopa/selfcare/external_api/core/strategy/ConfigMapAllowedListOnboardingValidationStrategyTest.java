package it.pagopa.selfcare.external_api.core.strategy;

import java.util.Map;
import java.util.Set;

import it.pagopa.selfcare.external_api.core.ConfigMapAllowedListOnboardingValidationStrategy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class ConfigMapAllowedListOnboardingValidationStrategyTest {
    ConfigMapAllowedListOnboardingValidationStrategyTest() {
    }

    @Test
    void validate_allowedListNotConfigured() {
        Map<String, Set<String>> institutionProductsAllowedList = null;
        String productId = "prod-io";
        String institutionExternalId = "inst-1";
        ConfigMapAllowedListOnboardingValidationStrategy validationStrategy = new ConfigMapAllowedListOnboardingValidationStrategy((Map)institutionProductsAllowedList);
        Executable executable = () -> {
            validationStrategy.validate("prod-io", "inst-1");
        };
        Assertions.assertDoesNotThrow(executable);
    }

    @Test
    void validate_productNotInConfig() {
        Map<String, Set<String>> institutionProductsAllowedList = Map.of();
        String productId = "prod-io";
        String institutionExternalId = "inst-1";
        ConfigMapAllowedListOnboardingValidationStrategy validationStrategy = new ConfigMapAllowedListOnboardingValidationStrategy(institutionProductsAllowedList);
        boolean validate = validationStrategy.validate("prod-io", "inst-1");
        Assertions.assertFalse(validate);
    }

    @Test
    void validate_invalidConfig_invalidUsageOfSpecialCharacter() {
        Map<String, Set<String>> institutionProductsAllowedList = Map.of("prod-io", Set.of("inst-1", "*"));
        Executable executable = () -> {
            new ConfigMapAllowedListOnboardingValidationStrategy(institutionProductsAllowedList);
        };
        IllegalArgumentException e = (IllegalArgumentException)Assertions.assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("Invalid configuration: bad using of special character '*' in allowed-list for key 'prod-io'. If used, the '*' is the only value allowed for a given key", e.getMessage());
    }

    @Test
    void validate_allowedListSizeGreaterThanOne() {
        Map<String, Set<String>> institutionProductsAllowedList = Map.of("prod-io", Set.of("inst-1", "inst-2", "inst-3"));
        String productId = "prod-io";
        String institutionExternalId = "inst-2";
        ConfigMapAllowedListOnboardingValidationStrategy validationStrategy = new ConfigMapAllowedListOnboardingValidationStrategy(institutionProductsAllowedList);
        boolean validate = validationStrategy.validate("prod-io", "inst-2");
        Assertions.assertTrue(validate);
    }

    @Test
    void validate_institutionExplicitlyInAllowed() {
        Map<String, Set<String>> institutionProductsAllowedList = Map.of("prod-io", Set.of("inst-1"));
        String productId = "prod-io";
        String institutionExternalId = "inst-1";
        ConfigMapAllowedListOnboardingValidationStrategy validationStrategy = new ConfigMapAllowedListOnboardingValidationStrategy(institutionProductsAllowedList);
        boolean validate = validationStrategy.validate("prod-io", "inst-1");
        Assertions.assertTrue(validate);
    }

    @Test
    void validate_institutionImplicitlyInAllowedList() {
        Map<String, Set<String>> institutionProductsDisallowedList = null;
        Map<String, Set<String>> institutionProductsAllowedList = Map.of("prod-io", Set.of("*"));
        String productId = "prod-io";
        String institutionExternalId = "inst-1";
        ConfigMapAllowedListOnboardingValidationStrategy validationStrategy = new ConfigMapAllowedListOnboardingValidationStrategy(institutionProductsAllowedList);
        boolean validate = validationStrategy.validate("prod-io", "inst-1");
        Assertions.assertTrue(validate);
    }

    @Test
    void validate_institutionNotInAllowedList() {
        Map<String, Set<String>> institutionProductsAllowedList = Map.of("prod-io", Set.of("inst-1"));
        String productId = "prod-io";
        String institutionExternalId = "inst-2";
        ConfigMapAllowedListOnboardingValidationStrategy validationStrategy = new ConfigMapAllowedListOnboardingValidationStrategy(institutionProductsAllowedList);
        boolean validate = validationStrategy.validate("prod-io", "inst-2");
        Assertions.assertFalse(validate);
    }
}
