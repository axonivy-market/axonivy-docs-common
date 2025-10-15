# AxonIvyGenericDocsFactory - Unified Factory Pattern

## Overview

The `AxonIvyGenericDocsFactory` eliminates code duplication across Aspose factory classes by providing a generic, thread-safe, and configurable factory pattern. This reduces factory implementation from ~80 lines to ~15 lines per factory.

## Code Duplication Elimination

### Before: Original WordFactory and CellFactory

Each factory class contained:
- Static license field
- Static initializer block  
- Complex `loadLicense()` method with error handling
- Thread safety concerns
- Duplicate error logging logic
- Separate implementations for each Aspose product

**Total per factory: ~80 lines of mostly duplicated code**

### After: Using AxonIvyGenericDocsFactory

Each factory class now contains:
- Single factory creation line
- Simple delegation methods
- All complexity handled by generic factory

**Total per factory: ~15 lines of code (80% reduction!)**

## Usage Patterns

### 1. Static Factory Pattern (Recommended)

```java
public final class WordFactory {
  
  private static final AxonIvyGenericDocsFactory.StaticFactory<DocumentConverter, License> FACTORY = 
    AxonIvyGenericDocsFactory.createIvyFactory(
      ThirdPartyLicenses::getDocumentFactoryLicense,
      stream -> {
        License license = new License();
        license.setLicense(stream);
        return license;
      },
      DocumentConverter::new
    );

  private WordFactory() {}

  public static DocumentConverter convert() { return FACTORY.convert(); }
  public static <T> T get(Supplier<T> supplier) { return FACTORY.get(supplier); }
  public static void run(Runnable run) { FACTORY.run(run); }
  public static void loadLicense() { FACTORY.loadLicense(); }
  public static boolean isLicensed() { return FACTORY.isLicensed(); }
  public static License getLicense() { return FACTORY.getLicense(); }
}
```

### 2. Instance-based Pattern

```java
AxonIvyGenericDocsFactory<DocumentConverter, License> factory = 
  AxonIvyGenericDocsFactory.<DocumentConverter, License>builder()
    .licenseStream(ThirdPartyLicenses::getDocumentFactoryLicense)
    .licenseInitializer(stream -> {
      License license = new License();
      license.setLicense(stream);
      return license;
    })
    .converter(DocumentConverter::new)
    .build();

// Use the factory
DocumentConverter converter = factory.convert();
```

## Key Benefits

### 1. Massive Code Reduction
- **80% less code** per factory implementation
- Eliminates ~65 lines of boilerplate per factory

### 2. Unified Error Handling
- Automatic Ivy.log() integration with fallback
- Consistent error handling across all factories
- Graceful fallback to evaluation mode

### 3. Thread Safety
- Double-checked locking pattern implemented correctly
- Thread-safe license initialization
- No race conditions

### 4. Easy Extension
Adding a new Aspose product factory is trivial:

```java
public final class PdfFactory {
  private static final AxonIvyGenericDocsFactory.StaticFactory<PdfConverter, com.aspose.pdf.License> FACTORY = 
    AxonIvyGenericDocsFactory.createIvyFactory(
      ThirdPartyLicenses::getDocumentFactoryLicense,
      stream -> {
        com.aspose.pdf.License license = new com.aspose.pdf.License();
        license.setLicense(stream);
        return license;
      },
      PdfConverter::new
    );
  
  // Same pattern as other factories...
}
```

### 5. Consistent API
All factories provide the same methods:
- `convert()` - Create converter for fluent API
- `get(Supplier<T>)` - Execute with license guarantee
- `run(Runnable)` - Run task with license guarantee
- `loadLicense()` - Force license reload
- `isLicensed()` - Check license status
- `getLicense()` - Get license instance

## Factory Creation Methods

### `createIvyFactory()` - Recommended
- Automatic Ivy.log() error handling
- Fallback to System.err for non-Ivy environments
- Immediate license loading (static pattern)

### `createStaticFactory()`
- Basic static factory pattern
- No automatic error logging integration
- For custom error handling scenarios

### `builder()` 
- Instance-based configuration
- Manual license loading control
- Advanced customization scenarios

## Migration Guide

### Step 1: Replace factory implementation

**Old WordFactory.java (80 lines):**
```java
public final class WordFactory {
  private static License license;
  static { loadLicense(); }
  
  public static void loadLicense() {
    if (license != null) return;
    try {
      InputStream in = ThirdPartyLicenses.getDocumentFactoryLicense();
      if (in != null) {
        license = new License();
        license.setLicense(in);
      }
    } catch (Exception e) {
      Ivy.log().error(e);
      license = null;
    }
  }
  
  // ... rest of methods
}
```

**New WordFactory.java (15 lines):**
```java
public final class WordFactory {
  private static final StaticFactory<DocumentConverter, License> FACTORY = 
    AxonIvyGenericDocsFactory.createIvyFactory(
      ThirdPartyLicenses::getDocumentFactoryLicense,
      stream -> { License license = new License(); license.setLicense(stream); return license; },
      DocumentConverter::new
    );

  public static DocumentConverter convert() { return FACTORY.convert(); }
  public static <T> T get(Supplier<T> supplier) { return FACTORY.get(supplier); }
  public static void run(Runnable run) { FACTORY.run(run); }
  // ... other delegation methods
}
```

### Step 2: Update imports
Add import for the generic factory:
```java
import com.axonivy.utils.axonivydocscommon.AxonIvyGenericDocsFactory;
```

### Step 3: Test compatibility
The API remains exactly the same, so existing client code continues to work without changes.

## Technical Features

- **Thread-safe license initialization** using double-checked locking
- **Lazy license loading** with automatic retry on failure  
- **Graceful degradation** to evaluation mode on license failures
- **Environment detection** for appropriate error logging
- **Type-safe generics** preventing runtime type errors
- **Fluent builder API** for advanced configuration
- **Memory efficient** with minimal object allocation

## Maintenance Benefits

1. **Single point of maintenance** for license logic
2. **Consistent behavior** across all Aspose factories  
3. **Easier testing** with centralized logic
4. **Reduced bug surface** due to less code duplication
5. **Simplified documentation** with unified patterns