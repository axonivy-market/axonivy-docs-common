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
        assertFromMethodReturnsValidConverter(converter.from(inputStream));
    }

    @Test
    void testFromFile() throws IOException {
        Path testFile = createTestFile();
        assertFromMethodReturnsValidConverter(converter.from(testFile.toFile()));
    }

    @Test
    void testFromFilePath() throws IOException {
        Path testFile = createTestFile();
        assertFromMethodReturnsValidConverter(converter.from(testFile.toString()));
    }

    @Test
    void testFromByteArray() {
        byte[] bytes = TEST_CONTENT.getBytes(StandardCharsets.UTF_8);
        assertFromMethodReturnsValidConverter(converter.from(bytes));
    }

    @Test
    void testInputExceptions() {
        InputStream failingStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Test exception");
            }
        };
        assertInputException(() -> converter.from(failingStream), "Failed to load document");
        assertInputException(() -> converter.from(new File("non-existent.txt")), "Failed to load document from file");
        converter.shouldThrowExceptionOnLoad = true;
        assertInputException(() -> converter.from(TEST_CONTENT.getBytes()), "Failed to load document from byte array");
    }

    @Test
    void testFormatConversion() {
        setupValidConverter();
        assertThat(converter.toPdf().targetFormat).isEqualTo(PDF_FORMAT);
        assertThat(converter.to(OTHER_FORMAT).targetFormat).isEqualTo(OTHER_FORMAT);
    }

    @Test
    void testAsBytes() {
        setupValidConverter();
        byte[] result = converter.asBytes();
        assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo(CONVERTED_CONTENT);
    }

    @Test
    void testAsFile() throws IOException {
        setupValidConverter();
        Path outputPath = tempDir.resolve("output.pdf");
        assertFileOutput(converter.asFile(outputPath.toString()), outputPath);
    }

    @Test
    void testAsFileWithFileObject() throws IOException {
        setupValidConverter();
        File outputFile = tempDir.resolve("output.pdf").toFile();
        assertFileOutput(converter.asFile(outputFile), outputFile.toPath());
    }

    @Test
    void testAsFileCreatesDirectories() {
        setupValidConverter();
        Path nestedPath = tempDir.resolve("deep").resolve("nested").resolve("output.pdf");
        File result = converter.asFile(nestedPath.toString());
        assertThat(result).exists();
        assertThat(nestedPath.getParent()).exists();
    }

    @Test
    void testAsInputStream() throws IOException {
        setupValidConverter();
        try (InputStream result = converter.asInputStream()) {
            String content = new String(result.readAllBytes(), StandardCharsets.UTF_8);
            assertThat(content).isEqualTo(CONVERTED_CONTENT);
        }
    }

    @Test
    void testCompleteFileWorkflow() throws IOException {
        Path sourceFile = createTestFile();
        Path outputFile = tempDir.resolve("output.pdf");
        File result = converter
            .from(sourceFile.toFile())
            .toPdf()
            .asFile(outputFile.toString());
        assertThat(result).exists();
        assertThat(Files.readString(outputFile, StandardCharsets.UTF_8)).isEqualTo(CONVERTED_CONTENT);
    }

    private Path createTestFile() throws IOException {
        Path testFile = tempDir.resolve("test.txt");
        Files.write(testFile, TEST_CONTENT.getBytes(StandardCharsets.UTF_8));
        return testFile;
    }
    
    private void setupValidConverter() {
        converter.document = TEST_CONTENT;
        converter.targetFormat = PDF_FORMAT;
    }

    private void assertFromMethodReturnsValidConverter(TestConverter result) {
        assertThat(result).isSameAs(converter);
        assertThat(converter.document).isEqualTo(TEST_CONTENT);
    }

    private void assertInputException(Runnable action, String expectedMessage) {
        assertThatThrownBy(action::run)
            .isInstanceOf(DocumentConversionException.class)
            .hasMessage(expectedMessage);
    }

    private void assertFileOutput(File result, Path outputPath) throws IOException {
        assertThat(result).exists();
        assertThat(Files.readString(outputPath, StandardCharsets.UTF_8)).isEqualTo(CONVERTED_CONTENT);
    }

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