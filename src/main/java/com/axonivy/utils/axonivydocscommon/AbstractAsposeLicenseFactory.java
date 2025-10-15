package com.axonivy.utils.axonivydocscommon;

import java.io.InputStream;
import java.util.function.Supplier;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Abstract base class for Aspose license management factories.
 * <p>
 * This class provides a generic implementation for license initialization and management
 * that can be shared across different Aspose products (Words, Cells, etc.).
 * </p>
 * 
 * @param <L> the license type (e.g., com.aspose.words.License, com.aspose.cells.License)
 * @param <C> the converter type (e.g., DocumentConverter, SpreadsheetConverter)
 */
public abstract class AbstractAsposeLicenseFactory<L, C> {
  
  /**
   * Thread-safe reference to the license instance.
   */
  private final AtomicReference<L> licenseRef = new AtomicReference<>();
  
  /**
   * Gets the current license instance.
   * @return the current license instance, or null if not initialized
   */
  protected L getLicense() {
    return licenseRef.get();
  }
  
  /**
   * Sets the license instance.
   * @param license the license instance to set
   */
  protected void setLicense(L license) {
    licenseRef.set(license);
  }
  
  /**
   * Creates a new license instance.
   * @return a new license instance
   */
  protected abstract L createLicense();
  
  /**
   * Sets the license on the license instance.
   * @param license the license instance
   * @param licenseStream the license stream
   * @throws LicenseInitializationException if license setting fails
   */
  protected abstract void setLicenseStream(L license, InputStream licenseStream) throws LicenseInitializationException;
  
  /**
   * Gets the license stream for this factory.
   * @return the license stream
   */
  protected abstract InputStream getLicenseStream();
  
  /**
   * Creates a new converter instance.
   * @return a new converter instance
   */
  protected abstract C createConverter();
  
  /**
   * Logs an error message.
   * @param e the exception to log
   */
  protected abstract void logError(Exception e);
  
  /**
   * Initializes the Aspose license for the application.
   * <p>
   * This method implements a singleton pattern to ensure the license is loaded
   * only once per application lifecycle. It retrieves the license from the
   * configured license stream and applies it to the Aspose License instance.
   * </p>
   * 
   * <p>
   * <strong>Behavior:</strong>
   * <ul>
   * <li>If license is already loaded, the method returns immediately</li>
   * <li>If license stream is available, creates and configures License instance</li>
   * <li>If license stream is null, leaves license as null (evaluation mode)</li>
   * <li>If any exception occurs, logs the error and resets license to null</li>
   * </ul>
   * </p>
   *
   * <p>
   * <strong>Note:</strong> When running in evaluation mode (license == null),
   * Aspose products will have functional limitations such as watermarks and document
   * size restrictions.
   * </p>
   */
  public void loadLicense() {
    // Check if license is already initialized to avoid redundant loading
    if (getLicense() != null) {
      return;
    }
    
    // Double-checked locking for thread safety
    synchronized (this) {
      if (getLicense() != null) {
        return;
      }
      
      try {
        // Attempt to retrieve license from the configured license stream
        InputStream licenseStream = getLicenseStream();
        if (licenseStream != null) {
          // Create and configure the Aspose license
          L licenseInstance = createLicense();
          setLicenseStream(licenseInstance, licenseStream);
          setLicense(licenseInstance);
        }
        // If license stream is null, license remains null (evaluation mode)
      } catch (Exception e) {
        // Log any licensing errors and reset license to null
        logError(e);
        setLicense(null);
      }
    }
  }
  
  /**
   * Creates a new converter for fluent API usage.
   * <p>
   * This factory method provides the main entry point for conversion operations.
   * The returned converter supports a fluent API pattern, allowing for intuitive 
   * chaining of conversion operations.
   * </p>
   * 
   * <p>
   * <strong>Note:</strong> The license is automatically managed by this factory,
   * so callers don't need to worry about license initialization.
   * </p>
   * 
   * @return a new converter instance ready for configuration
   */
  public C convert() {
    return createConverter();
  }
  
  /**
   * Executes a supplier function with guaranteed Aspose license initialization.
   * <p>
   * This utility method ensures that the Aspose license is properly loaded
   * before executing the provided {@link Supplier} function. It provides a safe
   * wrapper for operations that depend on licensed functionality without
   * requiring explicit license checks from the caller.
   * </p>
   *
   * <p>
   * <strong>Note:</strong> This method does not explicitly call
   * {@link #loadLicense()} as the license is already initialized in the static
   * block. However, it provides a semantic guarantee that licensing is handled
   * properly.
   * </p>
   *
   * @param <T>      the return type of the supplier function
   * @param supplier the function to execute, must not be null
   * @return the result produced by the supplier function
   * @throws NullPointerException if supplier is null
   */
  public <T> T get(Supplier<T> supplier) {
    return supplier.get();
  }
  
  /**
   * Executes a runnable task with guaranteed Aspose license initialization.
   * <p>
   * This utility method ensures that the Aspose license is properly loaded
   * before executing the provided {@link Runnable} task. It provides a safe
   * wrapper for void operations that depend on licensed functionality without
   * requiring explicit license management from the caller.
   * </p>
   *
   * <p>
   * <strong>Note:</strong> This method does not explicitly call
   * {@link #loadLicense()} as the license is already initialized in the static
   * block. However, it provides a semantic guarantee that licensing is handled
   * properly.
   * </p>
   *
   * @param run the task to execute, must not be null
   * @throws NullPointerException if run is null
   */
  public void run(Runnable run) {
    run.run();
  }
}