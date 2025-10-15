package com.axonivy.utils.axonivydocscommon;

/**
 * Exception thrown when license initialization fails.
 */
public class LicenseInitializationException extends RuntimeException {
  
  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new license initialization exception with the specified detail message.
   * 
   * @param message the detail message
   */
  public LicenseInitializationException(String message) {
    super(message);
  }

  /**
   * Constructs a new license initialization exception with the specified detail message and cause.
   * 
   * @param message the detail message
   * @param cause the cause
   */
  public LicenseInitializationException(String message, Throwable cause) {
    super(message, cause);
  }
}