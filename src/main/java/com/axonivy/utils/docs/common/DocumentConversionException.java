package com.axonivy.utils.docs.common;

/**
 * Exception thrown when Spreadsheet conversion operations fail. This is a
 * runtime exception that wraps underlying conversion errors.
 */
public class DocumentConversionException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new DocumentConversionException with the specified detail
   * message.
   * 
   * @param message the detail message
   */
  public DocumentConversionException(String message) {
    super(message);
  }

  /**
   * Constructs a new DocumentConversionException with the specified detail
   * message and cause.
   * 
   * @param message the detail message
   * @param cause   the cause
   */
  public DocumentConversionException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new DocumentConversionException with the specified cause.
   * 
   * @param cause the cause
   */
  public DocumentConversionException(Throwable cause) {
    super(cause);
  }
}