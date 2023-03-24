package it.pagopa.selfcare.external_api.web.model.product;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.external_api.web.model.products.ProductRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotBlank;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductRoleTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Test
    void validateNullFields() {
        // given
        HashMap<String, Class<? extends Annotation>> toCheckMap = new HashMap<>();
        toCheckMap.put("code", NotBlank.class);
        toCheckMap.put("label", NotBlank.class);
        toCheckMap.put("description", NotBlank.class);

        ProductRole productRole = new ProductRole();

        // when
        Set<ConstraintViolation<Object>> violations = validator.validate(productRole);
        // then
        List<ConstraintViolation<Object>> filteredViolations = violations.stream()
                .filter(violation -> {
                    Class<? extends Annotation> annotationToCheck = toCheckMap.get(violation.getPropertyPath().toString());
                    return !violation.getConstraintDescriptor().getAnnotation().annotationType().equals(annotationToCheck);
                })
                .collect(Collectors.toList());
        assertTrue(filteredViolations.isEmpty());
    }

    @Test
    void validateNotNullFields() {
        // given
        ProductRole productRole = TestUtils.mockInstance(new ProductRole());
        // when
        Set<ConstraintViolation<Object>> violations = validator.validate(productRole);
        // then
        assertTrue(violations.isEmpty());
    }
}
