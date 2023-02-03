package it.pagopa.selfcare.external_api.web.handler;

import it.pagopa.selfcare.commons.web.model.Problem;
import it.pagopa.selfcare.external_api.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;

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
}
