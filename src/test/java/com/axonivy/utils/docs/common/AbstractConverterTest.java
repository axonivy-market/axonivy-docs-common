package com.axonivy.utils.docs.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AbstractConverterTest {

    @TempDir
    Path tempDir;

    private TestConverter converter;
    private static final String TEST_CONTENT = "Test document content";
    private static final String CONVERTED_CONTENT = "CONVERTED: Test document content";
    private static final int PDF_FORMAT = 1;
    private static final int OTHER_FORMAT = 2;

    @BeforeEach
    void setUp() {
        converter = new TestConverter();
    }

    @Test
    void testFromInputStream() {
        InputStream inputStream = new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8));
        
        TestConverter result = converter.from(inputStream);
        
        assertThat(result).isSameAs(converter);
        assertThat(converter.document).isEqualTo(TEST_CONTENT);
    }

    @Test
    void testFromInputStreamWithException() {
        InputStream inputStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Test exception");
            }
        };
        
        assertThatThrownBy(() -> converter.from(inputStream))
            .isInstanceOf(DocumentConversionException.class)
            .hasMessage("Failed to load document")
            .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void testFromFile() throws IOException {
        Path testFile = tempDir.resolve("test.txt");
        Files.write(testFile, TEST_CONTENT.getBytes(StandardCharsets.UTF_8));
        
        TestConverter result = converter.from(testFile.toFile());
        
        assertThat(result).isSameAs(converter);
        assertThat(converter.document).isEqualTo(TEST_CONTENT);
    }

    @Test
    void testFromFileWithInvalidPath() {
        File nonExistentFile = new File("non-existent-file.txt");
        
        assertThatThrownBy(() -> converter.from(nonExistentFile))
            .isInstanceOf(DocumentConversionException.class)
            .hasMessage("Failed to load document from file");
    }

    @Test
    void testFromFilePath() throws IOException {
        Path testFile = tempDir.resolve("test.txt");
        Files.write(testFile, TEST_CONTENT.getBytes(StandardCharsets.UTF_8));
        
        TestConverter result = converter.from(testFile.toString());
        
        assertThat(result).isSameAs(converter);
        assertThat(converter.document).isEqualTo(TEST_CONTENT);
    }

    @Test
    void testFromFilePathWithInvalidPath() {
        String nonExistentPath = "non-existent-file.txt";
        
        assertThatThrownBy(() -> converter.from(nonExistentPath))
            .isInstanceOf(DocumentConversionException.class)
            .hasMessage("Failed to load document from path");
    }

    @Test
    void testFromByteArray() {
        byte[] bytes = TEST_CONTENT.getBytes(StandardCharsets.UTF_8);
        
        TestConverter result = converter.from(bytes);
        
        assertThat(result).isSameAs(converter);
        assertThat(converter.document).isEqualTo(TEST_CONTENT);
    }

    @Test
    void testFromByteArrayWithException() {
        converter.shouldThrowExceptionOnLoad = true;
        byte[] bytes = TEST_CONTENT.getBytes(StandardCharsets.UTF_8);
        
        assertThatThrownBy(() -> converter.from(bytes))
            .isInstanceOf(DocumentConversionException.class)
            .hasMessage("Failed to load document from byte array");
    }

    @Test
    void testToPdf() {
        converter.document = TEST_CONTENT;
        
        TestConverter result = converter.toPdf();
        
        assertThat(result).isSameAs(converter);
        assertThat(converter.targetFormat).isEqualTo(PDF_FORMAT);
    }

    @Test
    void testToFormat() {
        converter.document = TEST_CONTENT;
        
        TestConverter result = converter.to(OTHER_FORMAT);
        
        assertThat(result).isSameAs(converter);
        assertThat(converter.targetFormat).isEqualTo(OTHER_FORMAT);
    }

    @Test
    void testToFormatWithoutDocument() {
        assertThatThrownBy(() -> converter.to(PDF_FORMAT))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("No source document set. Call from() method first.");
    }

    @Test
    void testAsBytes() {
        converter.document = TEST_CONTENT;
        converter.targetFormat = PDF_FORMAT;
        
        byte[] result = converter.asBytes();
        
        assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo(CONVERTED_CONTENT);
    }

    @Test
    void testAsBytesWithoutDocument() {
        converter.targetFormat = PDF_FORMAT;
        
        assertThatThrownBy(() -> converter.asBytes())
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("No source document set. Call from() method first.");
    }

    @Test
    void testAsBytesWithoutTargetFormat() {
        converter.document = TEST_CONTENT;
        
        assertThatThrownBy(() -> converter.asBytes())
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("No target format set. Call to() or toPdf() method first.");
    }

    @Test
    void testAsBytesWithSaveException() {
        converter.document = TEST_CONTENT;
        converter.targetFormat = PDF_FORMAT;
        converter.shouldThrowExceptionOnSave = true;
        
        assertThatThrownBy(() -> converter.asBytes())
            .isInstanceOf(DocumentConversionException.class)
            .hasMessage("Failed to convert document");
    }

    @Test
    void testAsFile() throws IOException {
        converter.document = TEST_CONTENT;
        converter.targetFormat = PDF_FORMAT;
        Path outputPath = tempDir.resolve("output.pdf");
        
        File result = converter.asFile(outputPath.toString());
        
        assertThat(result.getAbsolutePath()).isEqualTo(outputPath.toString());
        assertThat(Files.exists(outputPath)).isTrue();
        String content = Files.readString(outputPath, StandardCharsets.UTF_8);
        assertThat(content).isEqualTo(CONVERTED_CONTENT);
    }

    @Test
    void testAsFileWithFileObject() throws IOException {
        converter.document = TEST_CONTENT;
        converter.targetFormat = PDF_FORMAT;
        File outputFile = tempDir.resolve("output.pdf").toFile();
        
        File result = converter.asFile(outputFile);
        
        assertThat(result.getAbsolutePath()).isEqualTo(outputFile.getAbsolutePath());
        assertThat(outputFile).exists();
        String content = Files.readString(outputFile.toPath(), StandardCharsets.UTF_8);
        assertThat(content).isEqualTo(CONVERTED_CONTENT);
    }

    @Test
    void testAsFileCreatesParentDirectories() throws IOException {
        converter.document = TEST_CONTENT;
        converter.targetFormat = PDF_FORMAT;
        Path nestedPath = tempDir.resolve("nested").resolve("deep").resolve("output.pdf");
        
        File result = converter.asFile(nestedPath.toString());
        
        assertThat(result).exists();
        assertThat(nestedPath.getParent()).exists();
        String content = Files.readString(nestedPath, StandardCharsets.UTF_8);
        assertThat(content).isEqualTo(CONVERTED_CONTENT);
    }

    @Test
    void testAsFileWithoutDocument() {
        converter.targetFormat = PDF_FORMAT;
        Path outputPath = tempDir.resolve("output.pdf");
        
        assertThatThrownBy(() -> {
            converter.asFile(outputPath.toString());
        })
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("No source document set. Call from() method first.");
    }

    @Test
    void testAsFileWithoutTargetFormat() {
        converter.document = TEST_CONTENT;
        Path outputPath = tempDir.resolve("output.pdf");
        
        assertThatThrownBy(() -> {
            converter.asFile(outputPath.toString());
        })
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("No target format set. Call to() or toPdf() method first.");
    }

    @Test
    void testAsFileWithSaveException() {
        converter.document = TEST_CONTENT;
        converter.targetFormat = PDF_FORMAT;
        converter.shouldThrowExceptionOnSave = true;
        Path outputPath = tempDir.resolve("output.pdf");
        
        assertThatThrownBy(() -> {
            converter.asFile(outputPath.toString());
        })
            .isInstanceOf(DocumentConversionException.class)
            .hasMessage("Failed to save converted document");
    }

    @Test
    void testAsInputStream() throws IOException {
        converter.document = TEST_CONTENT;
        converter.targetFormat = PDF_FORMAT;
        
        try (InputStream result = converter.asInputStream()) {
            String content = new String(result.readAllBytes(), StandardCharsets.UTF_8);
            assertThat(content).isEqualTo(CONVERTED_CONTENT);
        }
    }

    @Test
    void testAsInputStreamWithoutDocument() {
        converter.targetFormat = PDF_FORMAT;
        
        assertThatThrownBy(() -> converter.asInputStream())
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("No source document set. Call from() method first.");
    }

    @Test
    void testFluentChaining() {
        byte[] inputBytes = TEST_CONTENT.getBytes(StandardCharsets.UTF_8);
        
        byte[] result = converter
            .from(inputBytes)
            .toPdf()
            .asBytes();
        
        assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo(CONVERTED_CONTENT);
    }

    @Test
    void testCompleteWorkflowWithFile() throws IOException {
        // Create source file
        Path sourceFile = tempDir.resolve("source.txt");
        Files.write(sourceFile, TEST_CONTENT.getBytes(StandardCharsets.UTF_8));
        
        // Create output file
        Path outputFile = tempDir.resolve("output.pdf");
        
        // Convert
        File result = converter
            .from(sourceFile.toFile())
            .toPdf()
            .asFile(outputFile.toString());
        
        assertThat(result).exists();
        String content = Files.readString(outputFile, StandardCharsets.UTF_8);
        assertThat(content).isEqualTo(CONVERTED_CONTENT);
    }

    @Test
    void testParentDirectoryCreationFailure() {
        converter.document = TEST_CONTENT;
        converter.targetFormat = PDF_FORMAT;
        converter.shouldThrowExceptionOnSave = true;
        
        // Use a path that would require directory creation
        Path invalidPath = tempDir.resolve("nested").resolve("output.pdf");
        
        assertThatThrownBy(() -> {
            converter.asFile(invalidPath.toString());
        })
            .isInstanceOf(DocumentConversionException.class)
            .hasMessage("Failed to save converted document");
    }

    /**
     * Test implementation of AbstractConverter for testing purposes
     */
    static class TestConverter extends AbstractConverter<TestConverter, String> {
        boolean shouldThrowExceptionOnLoad = false;
        boolean shouldThrowExceptionOnSave = false;

        @Override
        protected String loadFromInputStream(InputStream inputStream) throws Exception {
            if (shouldThrowExceptionOnLoad) {
                throw new IOException("Test exception");
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }

        @Override
        protected String loadFromFile(String filePath) throws Exception {
            if (shouldThrowExceptionOnLoad) {
                throw new IOException("Test exception");
            }
            return Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
        }

        @Override
        protected void saveToStream(String document, ByteArrayOutputStream outputStream, int format) throws Exception {
            if (shouldThrowExceptionOnSave) {
                throw new IOException("Test save exception");
            }
            String content = "CONVERTED: " + document;
            outputStream.write(content.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        protected void saveToFile(String document, String outputPath, int format) throws Exception {
            if (shouldThrowExceptionOnSave) {
                throw new IOException("Test save exception");
            }
            
            String content = "CONVERTED: " + document;
            Files.write(Paths.get(outputPath), content.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        protected int getPdfFormat() {
            return PDF_FORMAT;
        }
    }
}