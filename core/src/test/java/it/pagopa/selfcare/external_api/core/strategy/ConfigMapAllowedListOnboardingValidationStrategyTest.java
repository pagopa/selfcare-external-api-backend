package it.pagopa.selfcare.external_api.core.strategy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigMapAllowedListOnboardingValidationStrategyTest {

    @Test
    void validate_allowedListNotConfigured() {
        // given
        final Map<String, Set<String>> institutionProductsAllowedList = null;
        final String productId = "prod-io";
        final String institutionExternalId = "inst-1";
        final ConfigMapAllowedListOnboardingValidationStrategy validationStrategy =
                new ConfigMapAllowedListOnboardingValidationStrategy(institutionProductsAllowedList);
        // when
        final Executable executable = () -> validationStrategy.validate(productId, institutionExternalId);
        // then
        assertDoesNotThrow(executable);
    }


    @Test
    void validate_productNotInConfig() {
        // given
        final Map<String, Set<String>> institutionProductsAllowedList = Map.of();
        final String productId = "prod-io";
        final String institutionExternalId = "inst-1";
        final ConfigMapAllowedListOnboardingValidationStrategy validationStrategy =
                new ConfigMapAllowedListOnboardingValidationStrategy(institutionProductsAllowedList);
        // when
        final boolean validate = validationStrategy.validate(productId, institutionExternalId);
        // then
        assertFalse(validate);
    }


    @Test
    void validate_invalidConfig_invalidUsageOfSpecialCharacter() {
        // given
        final Map<String, Set<String>> institutionProductsAllowedList = Map.of("prod-io", Set.of("inst-1", "*"));
        // when
        final Executable executable = () -> new ConfigMapAllowedListOnboardingValidationStrategy(institutionProductsAllowedList);
        // then
        final IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("Invalid configuration: bad using of special character '*' in allowed-list for key 'prod-io'. If used, the '*' is the only value allowed for a given key", e.getMessage());
    }


    @Test
    void validate_institutionExplicitlyInAllowed() {
        // given
        final Map<String, Set<String>> institutionProductsAllowedList =
                Map.of("prod-io", Set.of("inst-1"));
        final String productId = "prod-io";
        final String institutionExternalId = "inst-1";
        final ConfigMapAllowedListOnboardingValidationStrategy validationStrategy =
                new ConfigMapAllowedListOnboardingValidationStrategy(institutionProductsAllowedList);
        // when
        final boolean validate = validationStrategy.validate(productId, institutionExternalId);
        // then
        assertTrue(validate);
    }


    @Test
    void validate_institutionImplicitlyInAllowedList() {
        // given
        final Map<String, Set<String>> institutionProductsDisallowedList = null;
        final Map<String, Set<String>> institutionProductsAllowedList =
                Map.of("prod-io", Set.of("*"));
        final String productId = "prod-io";
        final String institutionExternalId = "inst-1";
        final ConfigMapAllowedListOnboardingValidationStrategy validationStrategy =
                new ConfigMapAllowedListOnboardingValidationStrategy(institutionProductsAllowedList);
        // when
        final boolean validate = validationStrategy.validate(productId, institutionExternalId);
        // then
        assertTrue(validate);
    }


    @Test
    void validate_institutionNotInAllowedList() {
        // given
        final Map<String, Set<String>> institutionProductsAllowedList =
                Map.of("prod-io", Set.of("inst-1"));
        final String productId = "prod-io";
        final String institutionExternalId = "inst-2";
        final ConfigMapAllowedListOnboardingValidationStrategy validationStrategy =
                new ConfigMapAllowedListOnboardingValidationStrategy(institutionProductsAllowedList);
        // when
        final boolean validate = validationStrategy.validate(productId, institutionExternalId);
        // then
        assertFalse(validate);
    }

}
