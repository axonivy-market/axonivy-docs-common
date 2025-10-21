# Axon Ivy Docs Common

[![CI Build](https://github.com/axonivy-market/axonivy-docs-common/actions/workflows/ci.yml/badge.svg)](https://github.com/axonivy-market/axonivy-docs-common/actions/workflows/ci.yml)

A common library that reduces effort to set up Aspose document converters for Axon Ivy projects. This library provides a unified, fluent API for document conversion operations, eliminating redundant code across different Axon Ivy document processing components.

## Overview

This library provides an abstract base class `AbstractConverter` that standardizes document conversion workflows across different document types (Word, Excel, PowerPoint, etc.) when using Aspose libraries. Instead of implementing conversion logic repeatedly in each project, you can extend this base class and focus only on the document-type-specific implementation details.

## Key Features

- **Fluent API**: Method chaining for intuitive document conversion workflows
- **Multiple Input Sources**: Support for InputStream, File, file path, and byte array inputs
- **Flexible Output Formats**: Convert to bytes, files, or InputStream
- **Unified Error Handling**: Consistent exception handling across all converter implementations
- **Generic Type Safety**: Type-safe implementation using Java generics