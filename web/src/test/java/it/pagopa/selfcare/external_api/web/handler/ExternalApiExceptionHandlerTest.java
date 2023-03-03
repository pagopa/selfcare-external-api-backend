package it.pagopa.selfcare.external_api.web.handler;

import it.pagopa.selfcare.commons.web.model.Problem;
import it.pagopa.selfcare.external_api.core.exception.OnboardingNotAllowedException;
import it.pagopa.selfcare.external_api.core.exception.UpdateNotAllowedException;
import it.pagopa.selfcare.external_api.exceptions.InstitutionAlreadyOnboardedException;
import it.pagopa.selfcare.external_api.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.*;

class ExternalApiExceptionHandlerTest {

    private static final String DETAIL_MESSAGE = "detail message";
    private final ExternalApiExceptionHandler handler;

    public ExternalApiExceptionHandlerTest() {
        this.handler = new ExternalApiExceptionHandler();
    }


    @Test
    void handleResourceNotFoundException() {
        //given
        ResourceNotFoundException exceptionMock = mock(ResourceNotFoundException.class);
        when(exceptionMock.getMessage())
                .thenReturn(DETAIL_MESSAGE);
        //when
        ResponseEntity<Problem> responseEntity = handler.handleNotFoundException(exceptionMock);
        //then
        assertNotNull(responseEntity);
        assertEquals(NOT_FOUND, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(DETAIL_MESSAGE, responseEntity.getBody().getDetail());
        assertEquals(NOT_FOUND.value(), responseEntity.getBody().getStatus());
    }

    @Test
    void handleUpdateNotAllowedException() {
        // given
        UpdateNotAllowedException mockException = mock(UpdateNotAllowedException.class);
        when(mockException.getMessage())
                .thenReturn(DETAIL_MESSAGE);
        // when
        ResponseEntity<Problem> responseEntity = handler.handleUpdateNotAllowedException(mockException);
        // then
        assertNotNull(responseEntity);
        assertEquals(CONFLICT, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(DETAIL_MESSAGE, responseEntity.getBody().getDetail());
        assertEquals(CONFLICT.value(), responseEntity.getBody().getStatus());
    }

    @Test
    void handleOnboardingNotAllowedException() {
        // given
        OnboardingNotAllowedException mockException = mock(OnboardingNotAllowedException.class);
        when(mockException.getMessage())
                .thenReturn(DETAIL_MESSAGE);
        // when
        ResponseEntity<Problem> responseEntity = handler.handleOnboardingNotAllowedException(mockException);
        // then
        assertNotNull(responseEntity);
        assertEquals(FORBIDDEN, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(DETAIL_MESSAGE, responseEntity.getBody().getDetail());
        assertEquals(FORBIDDEN.value(), responseEntity.getBody().getStatus());
    }

    @Test
    void handleInstitutionAlreadyOnboardedException() {
        // given
        InstitutionAlreadyOnboardedException mockException = mock(InstitutionAlreadyOnboardedException.class);
        when(mockException.getMessage())
                .thenReturn(DETAIL_MESSAGE);
        // when
        ResponseEntity<Problem> responseEntity = handler.handleInstitutionAlreadyOnboardedException(mockException);
        // then
        assertNotNull(responseEntity);
        assertEquals(CONFLICT, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(DETAIL_MESSAGE, responseEntity.getBody().getDetail());
        assertEquals(CONFLICT.value(), responseEntity.getBody().getStatus());
    }


}
