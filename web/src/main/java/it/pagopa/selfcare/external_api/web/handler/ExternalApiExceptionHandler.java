package it.pagopa.selfcare.external_api.web.handler;

import it.pagopa.selfcare.commons.web.model.Problem;
import it.pagopa.selfcare.commons.web.model.mapper.ProblemMapper;
import it.pagopa.selfcare.external_api.core.exception.OnboardingNotAllowedException;
import it.pagopa.selfcare.external_api.core.exception.UpdateNotAllowedException;
import it.pagopa.selfcare.external_api.exceptions.InstitutionAlreadyOnboardedException;
import it.pagopa.selfcare.external_api.exceptions.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestControllerAdvice
public class ExternalApiExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<Problem> handleNotFoundException(Exception e) {
        log.warn(e.toString());
        return ProblemMapper.toResponseEntity(new Problem(NOT_FOUND, e.getMessage()));
    }

    @ExceptionHandler({OnboardingNotAllowedException.class})
    ResponseEntity<Problem> handleOnboardingNotAllowedException(OnboardingNotAllowedException e) {
        log.warn(e.toString());
        return ProblemMapper.toResponseEntity(new Problem(FORBIDDEN, e.getMessage()));
    }

    @ExceptionHandler({UpdateNotAllowedException.class})
    ResponseEntity<Problem> handleUpdateNotAllowedException(UpdateNotAllowedException e) {
        log.warn(e.toString());
        return ProblemMapper.toResponseEntity(new Problem(CONFLICT, e.getMessage()));
    }

    @ExceptionHandler({InstitutionAlreadyOnboardedException.class})
    ResponseEntity<Problem> handleInstitutionAlreadyOnboardedException(InstitutionAlreadyOnboardedException e) {
        log.warn(e.toString());
        return ProblemMapper.toResponseEntity(new Problem(CONFLICT, e.getMessage()));
    }
}
