package com.axonivy.utils.docs.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;

class DocumentConversionExceptionTest {
    @Test
    void testConstructorWithMessage() {
        String message = "Test error message";
        DocumentConversionException exception = new DocumentConversionException(message);
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Test error message";
        IOException cause = new IOException("IO error");
        DocumentConversionException exception = new DocumentConversionException(message, cause);
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    void testConstructorWithCause() {
        IOException cause = new IOException("IO error");
        DocumentConversionException exception = new DocumentConversionException(cause);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getMessage()).isEqualTo("java.io.IOException: IO error");
    }

    @Test
    void testIsRuntimeException() {
        DocumentConversionException exception = new DocumentConversionException("Test message");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void testSerialVersionUID() {
        DocumentConversionException exception = new DocumentConversionException("Test");
        assertThat(exception).isInstanceOf(java.io.Serializable.class);
    }
}