package it.pagopa.selfcare.external_api.web.model.institutions;

import it.pagopa.selfcare.commons.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CompanyInformationsResourceTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Test
    void validateNotNullFields() {
        // given
        CompanyInformationsResource companyInformationsResource = TestUtils.mockInstance(new CompanyInformationsResource());
        // when
        Set<ConstraintViolation<Object>> violations = validator.validate(companyInformationsResource);
        // then
        assertTrue(violations.isEmpty());
    }

}
