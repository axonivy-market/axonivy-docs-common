package com.axonivy.utils.axonivydocscommon;

import java.io.InputStream;
import java.util.function.Supplier;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Generic base class for Aspose license management factories.
 * <p>
 * This class provides a template for license initialization and management
 * that can be shared across different Aspose products (Words, Cells, etc.).
 * </p>
 */
public class AsposeLicenseFactory {
  
  /**
   * Generic license factory implementation.
   * 
   * @param <L> the license type
   * @param <C> the converter type
   */
  public static class GenericFactory<L, C> {
    private final AtomicReference<L> licenseRef = new AtomicReference<>();
    private final LicenseConfiguration<L> licenseConfig;
    private final ConverterFactory<C> converterFactory;
    
    public GenericFactory(LicenseConfiguration<L> licenseConfig, ConverterFactory<C> converterFactory) {
      this.licenseConfig = licenseConfig;
      this.converterFactory = converterFactory;
    }
    
    /**
     * Gets the current license instance.
     */
    protected L getLicense() {
      return licenseRef.get();
    }
    
    /**
     * Sets the license instance.
     */
    protected void setLicense(L license) {
      licenseRef.set(license);
    }
    
    /**
     * Loads the license for this factory.
     */
    public void loadLicense() {
      if (getLicense() != null) {
        return;
      }
      
      synchronized (this) {
        if (getLicense() != null) {
          return;
        }
        
        try {
          InputStream licenseStream = licenseConfig.getLicenseStream();
          if (licenseStream != null) {
            L licenseInstance = licenseConfig.createLicense();
            licenseConfig.setLicenseStream(licenseInstance, licenseStream);
            setLicense(licenseInstance);
          }
        } catch (Exception e) {
          licenseConfig.logError(e);
          setLicense(null);
        }
      }
    }
    
    /**
     * Creates a new converter instance.
     */
    public C convert() {
      return converterFactory.createConverter();
    }
    
    /**
     * Executes a supplier with license guarantees.
     */
    public <T> T get(Supplier<T> supplier) {
      return supplier.get();
    }
    
    /**
     * Executes a runnable with license guarantees.
     */
    public void run(Runnable run) {
      run.run();
    }
  }
  
  /**
   * Configuration interface for license management.
   */
  public interface LicenseConfiguration<L> {
    L createLicense();
    void setLicenseStream(L license, InputStream licenseStream) throws LicenseInitializationException;
    InputStream getLicenseStream();
    void logError(Exception e);
  }
  
  /**
   * Factory interface for converter creation.
   */
  public interface ConverterFactory<C> {
    C createConverter();
  }
}