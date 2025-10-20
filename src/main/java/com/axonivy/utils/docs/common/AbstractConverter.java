package com.axonivy.utils.docs.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;


/**
 * Abstract base class for document conversion operations. Provides a fluent API
 * with common functionality for converting documents from one format to another.
 * 
 * @param <C> the type of the concrete converter (for method chaining)
 * @param <D> the type of the document object (e.g., Workbook, Document)
 */
public abstract class AbstractConverter<C extends AbstractConverter<C, D>, D> {
  protected D document;
  protected Integer targetFormat;

  /**
   * Creates a new AbstractConverter instance.
   */
  protected AbstractConverter() {
  }

  /**
   * Sets the source document from an InputStream.
   * 
   * @param inputStream the input stream containing the document data
   * @return this converter instance for method chaining
   * @throws E if document loading fails
   */
  public C from(InputStream inputStream) {
    try {
      this.document = loadFromInputStream(inputStream);
      return self();
    } catch (Exception e) {
      throw new DocumentConversionException("Failed to load document", e);
    }
  }

  /**
   * Sets the source document from a File.
   * 
   * @param file the file containing the document
   * @return this converter instance for method chaining
   * @throws E if document loading fails
   */
  public C from(File file) {
    try {
      this.document = loadFromFile(file.getAbsolutePath());
      return self();
    } catch (Exception e) {
      throw new DocumentConversionException("Failed to load document from file", e);
    }
  }

  /**
   * Sets the source document from a file path.
   * 
   * @param filePath the path to the file containing the document
   * @return this converter instance for method chaining
   * @throws E if document loading fails
   */
  public C from(String filePath) {
    try {
      this.document = loadFromFile(filePath);
      return self();
    } catch (Exception e) {
      throw new DocumentConversionException("Failed to load document from path", e);
    }
  }

  /**
   * Sets the source document from a byte array.
   * 
   * @param bytes the byte array containing the document data
   * @return this converter instance for method chaining
   * @throws E if document loading fails
   */
  public C from(byte[] bytes) {
    try {
      this.document = loadFromInputStream(new ByteArrayInputStream(bytes));
      return self();
    } catch (Exception e) {
      throw new DocumentConversionException("Failed to load document from byte array", e);
    }
  }

  /**
   * Converts the document to PDF format.
   * 
   * @return this converter instance for method chaining
   */
  public C toPdf() {
    return to(getPdfFormat());
  }

  /**
   * Converts the document to the specified format.
   * 
   * @param format the target format
   * @return this converter instance for method chaining
   */
  public C to(int format) {
    if (document == null) {
      throw new IllegalStateException("No source document set. Call from() method first.");
    }
    this.targetFormat = format;
    return self();
  }

  /**
   * Converts the document and returns the result as a byte array.
   * 
   * @return the converted document as byte array
   * @throws E if conversion fails
   */
  public byte[] asBytes() {
    validateConversionReady();
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      saveToStream(document, outputStream, targetFormat);
      return outputStream.toByteArray();
    } catch (Exception e) {
      throw new DocumentConversionException("Failed to convert document", e);
    }
  }

  /**
   * Converts the document and saves it as a file.
   * 
   * @param outputPath the path where the converted file should be saved
   * @return the File object representing the saved file
   * @throws E if conversion or file saving fails
   */
  public File asFile(String outputPath) {
    validateConversionReady();
    try {
      File outputFile = new File(outputPath);
      // Ensure parent directories exist
      File parentDir = outputFile.getParentFile();
      if (parentDir != null && !parentDir.exists()) {
        if (!parentDir.mkdirs() && !parentDir.exists()) {
          throw new DocumentConversionException("Failed to create parent directory: " + parentDir.getAbsolutePath(), null);
        }
      }

      saveToFile(document, outputPath, targetFormat);
      return outputFile;
    } catch (Exception e) {
      throw new DocumentConversionException("Failed to save converted document", e);
    }
  }

  /**
   * Converts the document and saves it as a file.
   * 
   * @param outputFile the File object where the converted document should be saved
   * @return the File object representing the saved file
   * @throws E if conversion or file saving fails
   */
  public File asFile(File outputFile) {
    return asFile(outputFile.getAbsolutePath());
  }

  /**
   * Converts the document and returns it as an InputStream. Note: The caller
   * is responsible for closing the returned InputStream.
   * 
   * @return an InputStream containing the converted document data
   * @throws E if conversion fails
   */
  public InputStream asInputStream() {
    byte[] bytes = asBytes();
    return new ByteArrayInputStream(bytes);
  }

  /**
   * Validates that the converter is ready for conversion.
   * 
   * @throws IllegalStateException if document or target format is not set
   */
  protected void validateConversionReady() {
    if (document == null) {
      throw new IllegalStateException("No source document set. Call from() method first.");
    }
    if (targetFormat == null) {
      throw new IllegalStateException("No target format set. Call to() or toPdf() method first.");
    }
  }

  /**
   * Returns this instance cast to the concrete type for method chaining.
   * 
   * @return this converter instance
   */
  @SuppressWarnings("unchecked")
  protected C self() {
    return (C) this;
  }

  // Abstract methods that subclasses must implement

  /**
   * Loads a document from an InputStream.
   * 
   * @param inputStream the input stream to load from
   * @return the loaded document
   * @throws Exception if loading fails
   */
  protected abstract D loadFromInputStream(InputStream inputStream) throws Exception;

  /**
   * Loads a document from a file path.
   * 
   * @param filePath the file path to load from
   * @return the loaded document
   * @throws Exception if loading fails
   */
  protected abstract D loadFromFile(String filePath) throws Exception;

  /**
   * Saves the document to an OutputStream.
   * 
   * @param document the document to save
   * @param outputStream the output stream to save to
   * @param format the format to save in
   * @throws Exception if saving fails
   */
  protected abstract void saveToStream(D document, ByteArrayOutputStream outputStream, int format) throws Exception;

  /**
   * Saves the document to a file.
   * 
   * @param document the document to save
   * @param outputPath the file path to save to
   * @param format the format to save in
   * @throws Exception if saving fails
   */
  protected abstract void saveToFile(D document, String outputPath, int format) throws Exception;

  /**
   * Gets the PDF format constant for this converter type.
   * 
   * @return the PDF format constant
   */
  protected abstract int getPdfFormat();
}