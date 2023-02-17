package it.pagopa.selfcare.external_api.web.model.institutions;

import it.pagopa.selfcare.commons.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PspDataResourceTest {

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
        toCheckMap.put("businessRegisterNumber", NotBlank.class);
        toCheckMap.put("legalRegisterName", NotBlank.class);
        toCheckMap.put("legalRegisterNumber", NotBlank.class);
        toCheckMap.put("abiCode", NotBlank.class);
        toCheckMap.put("vatNumberGroup", NotNull.class);

        PspDataResource pspDataResource = new PspDataResource();

        // when
        Set<ConstraintViolation<Object>> violations = validator.validate(pspDataResource);
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
        PspDataResource pspDataResource = TestUtils.mockInstance(new PspDataResource());
        // when
        Set<ConstraintViolation<Object>> violations = validator.validate(pspDataResource);
        // then
        assertTrue(violations.isEmpty());
    }

}
