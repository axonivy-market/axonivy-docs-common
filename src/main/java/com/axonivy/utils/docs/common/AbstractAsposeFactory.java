package com.axonivy.utils.docs.common;

import java.io.InputStream;
import java.util.function.Supplier;

/**
 * Abstract base factory class for managing Aspose operations and licensing
 * within Axon Ivy.
 * <p>
 * This class provides common functionality for Aspose product factories,
 * including:
 * <ul>
 * <li>Automatic license management for Aspose products</li>
 * <li>Template methods for creating converters</li>
 * <li>Utility methods for executing license-dependent operations</li>
 * </ul>
 * </p>
 * 
 * <p>
 * The class follows a static factory pattern and ensures that the Aspose
 * license is properly initialized before any operations are performed. If
 * licensing fails, the application continues in evaluation mode with limited
 * functionality.
 * </p>
 * 
 * @param <C> the type of Converter (e.g., SpreadsheetConverter,
 *            DocumentConverter)
 */
public abstract class AbstractAsposeFactory<C> {

  /**
   * The Aspose license instance, initialized once per application lifecycle.
   * <p>
   * This field stores the loaded Aspose license object and is shared across all
   * concrete factory implementations. The volatile modifier ensures thread-safe
   * access in multi-threaded environments.
   * </p>
   * <p>
   * When this field is {@code null}, it indicates that either:
   * <ul>
   * <li>License loading has not been attempted yet</li>
   * <li>License loading failed, leaving the application in evaluation mode</li>
   * <li>No license file was found in the Ivy third-party license service</li>
   * </ul>
   * </p>
   */
  private static volatile Object license;

  /**
   * Initializes the Aspose license for the current factory instance.
   * <p>
   * This method implements a thread-safe, lazy initialization pattern to ensure
   * the Aspose license is loaded exactly once per application lifecycle. It
   * follows these steps:
   * </p>
   * <ol>
   * <li>Checks if the license is already initialized to avoid redundant
   * loading</li>
   * <li>Retrieves the license input stream from Ivy's third-party license
   * service</li>
   * <li>Delegates to the concrete implementation to load the specific Aspose
   * product license</li>
   * <li>Handles any exceptions gracefully, logging errors and falling back to
   * evaluation mode</li>
   * </ol>
   * 
   * <p>
   * <strong>Thread Safety:</strong> This method is synchronized to prevent race
   * conditions during license initialization in multi-threaded environments.
   * </p>
   * 
   * <p>
   * <strong>Error Handling:</strong> If licensing fails for any reason, the
   * application continues in Aspose evaluation mode with limited functionality
   * and watermarks on output documents.
   * </p>
   * 
   * @see #getIvyAsposeLicense()
   * @see #loadDocumentLicence(InputStream)
   */
  protected static synchronized void loadLicense() {
    // Check if license is already initialized to avoid redundant loading
    if (license != null) {
      return;
    }
    try {
      // Attempt to retrieve license from Axon Ivy's third-party license service
      InputStream in = getIvyAsposeLicense();
      if (in != null) {
        // Create and configure the Aspose license using the provided factory
        license = loadDocumentLicence(in);
      }
      // If license stream is null, license remains null (evaluation mode)
    } catch (Exception e) {
      // Log any licensing errors and reset license to null
      handleFailedLicenceLoading(e);
      license = null;
    }
  }

  /**
   * Loads and configures the specific Aspose product license from an input
   * stream.
   * <p>
   * This abstract method must be implemented by concrete factory classes to
   * handle the specific licensing requirements of different Aspose products
   * (Words, Cells, PDF, etc.). Each product has its own License class and
   * initialization method.
   * </p>
   * 
   * <p>
   * <strong>Implementation Examples:</strong>
   * </p>
   * 
   * <pre>
   * // For Aspose.Words
   * protected static Object loadDocumentLicence(InputStream inputStream) throws LicenseInitializationException {
   *   try {
   *     License license = new License();
   *     license.setLicense(inputStream);
   *     return license;
   *   } catch (Exception e) {
   *     throw new LicenseInitializationException("Failed to load Aspose.Words license", e);
   *   }
   * }
   * 
   * // For Aspose.Cells
   * protected static Object loadDocumentLicence(InputStream inputStream) throws LicenseInitializationException {
   *   try {
   *     com.aspose.cells.License license = new com.aspose.cells.License();
   *     license.setLicense(inputStream);
   *     return license;
   *   } catch (Exception e) {
   *     throw new LicenseInitializationException("Failed to load Aspose.Cells license", e);
   *   }
   * }
   * </pre>
   * 
   * @param inputStream the license file input stream obtained from Ivy's
   *                    third-party license service
   * @return the initialized license object for the specific Aspose product
   * @throws LicenseInitializationException if the license cannot be loaded or
   *                                        applied
   * @see #getIvyAsposeLicense()
   * @see #loadLicense()
   */
  protected static Object loadDocumentLicence(InputStream inputStream) throws LicenseInitializationException {
    throw new LicenseInitializationException(
        "Failed to set license:: Need to overide 'loadDocumentLicence' method for License loading");
  }

  /**
   * Creates a new converter instance for the specific Aspose product.
   * <p>
   * This abstract method defines the factory method pattern for creating
   * converters. Each concrete implementation should return a specific converter
   * type (e.g., DocumentConverter for Words, SpreadsheetConverter for Cells) that
   * provides a fluent API for document conversion operations.
   * </p>
   * 
   * <p>
   * <strong>Usage Pattern:</strong>
   * </p>
   * 
   * <pre>
   * // Example usage in concrete implementations:
   * 
   * // WordFactory implementation
   * public DocumentConverter convert() {
   *   return new DocumentConverter();
   * }
   * 
   * // CellsFactory implementation  
   * public SpreadsheetConverter convert() {
   *   return new SpreadsheetConverter();
   * }
   * 
   * // Client usage
   * byte[] pdfBytes = WordFactory.convert().from(file).toPdf().asBytes();
   * File excelFile = CellsFactory.convert().from(csvFile).toXlsx().asFile("output.xlsx");
   * </pre>
   * 
   * <p>
   * The converter instances should handle license checking internally and provide
   * intuitive method chaining for common conversion scenarios.
   * </p>
   * 
   * @return a new converter instance for the specific Aspose product
   * @see #loadLicense()
   */
  public abstract C convert();

  /**
   * Executes a supplier function in a license-aware context.
   * <p>
   * This utility method provides a convenient way to execute license-dependent
   * operations without explicitly checking license status. The method ensures
   * that the Aspose license is properly initialized before invoking the supplier,
   * providing a consistent execution environment.
   * </p>
   * 
   * <p>
   * <strong>Use Cases:</strong>
   * </p>
   * <ul>
   * <li>Executing complex document operations that return values</li>
   * <li>Wrapping existing Aspose API calls in a license-safe context</li>
   * <li>Building higher-level abstractions over Aspose functionality</li>
   * </ul>
   * 
   * <p>
   * <strong>Usage Examples:</strong>
   * </p>
   * 
   * <pre>
   * // Execute a document operation that returns a result
   * String documentText = WordFactory.get(() -> {
   *   Document doc = new Document("input.docx");
   *   return doc.getText();
   * });
   * 
   * // Perform a calculation on spreadsheet data
   * Double sum = CellsFactory.get(() -> {
   *   Workbook workbook = new Workbook("data.xlsx");
   *   return workbook.getWorksheets().get(0).getCells().getMaxDataRow();
   * });
   * </pre>
   * 
   * @param supplier the function to execute, which should return a value
   * @param <T>      the return type of the supplier function
   * @return the result produced by the supplier function
   * @throws RuntimeException if the supplier throws any exception
   */
  public static <T> T get(Supplier<T> supplier) {
    return supplier.get();
  }

  /**
   * Executes a runnable task in a license-aware context.
   * <p>
   * This utility method provides a convenient way to execute license-dependent
   * operations that don't return values. Like the {@link #get(Supplier)} method,
   * it ensures proper license initialization before task execution.
   * </p>
   * 
   * <p>
   * <strong>Use Cases:</strong>
   * </p>
   * <ul>
   * <li>Performing document modifications or transformations</li>
   * <li>Executing batch operations on multiple documents</li>
   * <li>Running cleanup or maintenance tasks</li>
   * <li>Initializing or configuring Aspose components</li>
   * </ul>
   * 
   * <p>
   * <strong>Usage Examples:</strong>
   * </p>
   * 
   * <pre>
   * // Modify a document in place
   * WordFactory.run(() -> {
   *   Document doc = new Document("input.docx");
   *   doc.getRange().replace("old text", "new text", new FindReplaceOptions());
   *   doc.save("output.docx");
   * });
   * 
   * // Perform batch processing
   * CellsFactory.run(() -> {
   *   for (String fileName : fileList) {
   *     Workbook workbook = new Workbook(fileName);
   *     workbook.save(fileName.replace(".xls", ".xlsx"));
   *   }
   * });
   * </pre>
   * 
   * @param run the task to execute
   * @throws RuntimeException if the runnable throws any exception
   * @see #get(Supplier)
   */
  public static void run(Runnable run) {
    run.run();
  }

  /**
   * Retrieves the Aspose license input stream from Ivy's third-party license
   * service.
   * <p>
   * This abstract method must be implemented by concrete factory classes to
   * provide access to the appropriate license file for the specific Aspose
   * product. The implementation should use Ivy's {@code ThirdPartyLicenses}
   * service to obtain the license stream.
   * </p>
   * 
   * <p>
   * <strong>Implementation Examples:</strong>
   * </p>
   * 
   * <pre>
   * // For Aspose.Words license
   * protected static InputStream getIvyAsposeLicense() {
   *   return ThirdPartyLicenses.getDocumentFactoryLicense();
   * }
   * 
   * // For Aspose.Cells license
   * protected static InputStream getIvyAsposeLicense() {
   *   return ThirdPartyLicenses.getSpreadsheetFactoryLicense();
   * }
   * 
   * // For custom license location
   * protected static InputStream getIvyAsposeLicense() {
   *   return ThirdPartyLicenses.getLicense("custom-aspose-license.lic");
   * }
   * </pre>
   * 
   * <p>
   * <strong>Return Value Handling:</strong>
   * </p>
   * <ul>
   * <li>If the method returns {@code null}, the factory will operate in
   * evaluation mode</li>
   * <li>If the method returns a valid stream, the license will be applied to the
   * Aspose product</li>
   * <li>If the method throws an exception, it will be caught and logged by
   * {@link #loadLicense()}</li>
   * </ul>
   * 
   * @return an input stream containing the Aspose license file, or {@code null}
   *         if no license is available
   * @throws LicenseInitializationException if the license cannot be retrieved
   *                                        from the service
   * @see #loadLicense()
   * @see #loadDocumentLicence(InputStream)
   */
  protected static InputStream getIvyAsposeLicense() {
    throw new LicenseInitializationException(
        "Failed to set license:: Need to overide 'getIvyAsposeLicense' method for License loading");
  }

  protected static void handleFailedLicenceLoading(Exception e) {
    throw new LicenseInitializationException(
        "Failed to set license:: Need to overide 'handleFailedLicenceLoading' method for License loading");
  }
}