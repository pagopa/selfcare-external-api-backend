package it.pagopa.selfcare.external_api.web.model.institutions;

import it.pagopa.selfcare.commons.utils.TestUtils;
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

class DpoDataResourceTest {

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
        toCheckMap.put("address", NotBlank.class);
        toCheckMap.put("pec", NotBlank.class);
        toCheckMap.put("email", NotBlank.class);

        DpoDataResource dpoDataResource = new DpoDataResource();

        // when
        Set<ConstraintViolation<Object>> violations = validator.validate(dpoDataResource);
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
        DpoDataResource dpoDataResource = TestUtils.mockInstance(new DpoDataResource());
        dpoDataResource.setPec("dpoPec@example.com");
        dpoDataResource.setEmail("dpoEmail@example.com");
        // when
        Set<ConstraintViolation<Object>> violations = validator.validate(dpoDataResource);
        // then
        assertTrue(violations.isEmpty());
    }

}
