package com.investment.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void handleRuntimeException_notFoundMessage_returns404() {
        RuntimeException ex = new RuntimeException("用户不存在");

        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleRuntimeException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("用户不存在", response.getBody().get("message"));
    }

    @Test
    void handleRuntimeException_conflictMessage_returns409() {
        RuntimeException ex = new RuntimeException("用户名已存在");

        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleRuntimeException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("用户名已存在", response.getBody().get("message"));
    }

    @Test
    void handleRuntimeException_genericMessage_returns400() {
        RuntimeException ex = new RuntimeException("Something went wrong");

        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleRuntimeException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Something went wrong", response.getBody().get("message"));
    }

    @Test
    void handleUsernameNotFoundException_returns404() {
        UsernameNotFoundException ex = new UsernameNotFoundException("User not found");

        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleUsernameNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody().get("message"));
    }

    @Test
    void handleBadCredentialsException_returns401() {
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");

        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleBadCredentialsException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("用户名或密码错误", response.getBody().get("message")); // User-friendly message
    }

    @Test
    void handleGenericException_returns500() {
        Exception ex = new RuntimeException("Unexpected error");

        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("服务器内部错误", response.getBody().get("message")); // User-friendly, no leak
    }
}
