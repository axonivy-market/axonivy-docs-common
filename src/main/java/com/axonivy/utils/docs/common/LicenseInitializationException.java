package com.axonivy.utils.docs.common;

/**
 * Exception thrown when license initialization fails.
 */
public class LicenseInitializationException extends RuntimeException {
  
  private static final long serialVersionUID = 1L;

  public LicenseInitializationException(String message) {
    super(message);
  }

  public LicenseInitializationException(String message, Throwable cause) {
    super(message, cause);
  }
}