package com.codeanalyzer.splitter;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.segment.TextSegment;

import java.util.List;

/**
 * Static entry points, mirroring the ergonomics of langchain4j's own
 * {@code DocumentSplitters.recursive(...)} and Python's
 * {@code RecursiveCharacterTextSplitter.from_language(...)}.
 *
 * <pre>{@code
 * // Python:
 * // RecursiveCharacterTextSplitter.from_language(language=Language.JAVA, chunk_size=600, chunk_overlap=30)
 *
 * // Java, same intent:
 * DocumentSplitter splitter = CodeSplitters.recursive(Language.JAVA, 600, 30);
 * }</pre>
 */
public final class CodeSplitters {

    private CodeSplitters() {
    }

    /** One fixed language for every document passed through this splitter. */
    public static DocumentSplitter recursive(Language language, int chunkSize, int chunkOverlap) {
        return new CodeDocumentSplitter(language, chunkSize, chunkOverlap);
    }

    /**
     * Detects the language per-document from a metadata key (default {@code "file_name"}) —
     * useful when ingesting a whole repository of mixed file types through one pipeline.
     * Falls back to {@link Language#TEXT} if the key is missing or unrecognized.
     */
    public static DocumentSplitter recursiveAutoDetect(int chunkSize, int chunkOverlap) {
        return new AutoLanguageDocumentSplitter("file_name", chunkSize, chunkOverlap, Language.TEXT);
    }

    /** Same as {@link #recursiveAutoDetect(int, int)} with a custom metadata key and fallback language. */
    public static DocumentSplitter recursiveAutoDetect(String fileNameMetadataKey, int chunkSize, int chunkOverlap, Language fallback) {
        return new AutoLanguageDocumentSplitter(fileNameMetadataKey, chunkSize, chunkOverlap, fallback);
    }

    /**
     * Delegating {@link DocumentSplitter} that reads a per-document metadata field to pick
     * the {@link Language}, then hands off to {@link CodeDocumentSplitter}. Kept as a
     * nested static class here so the whole toolkit is three files.
     */
    private static class AutoLanguageDocumentSplitter implements DocumentSplitter {

        private final String fileNameMetadataKey;
        private final int chunkSize;
        private final int chunkOverlap;
        private final Language fallbackLanguage;

        AutoLanguageDocumentSplitter(String fileNameMetadataKey, int chunkSize, int chunkOverlap, Language fallbackLanguage) {
            this.fileNameMetadataKey = fileNameMetadataKey;
            this.chunkSize = chunkSize;
            this.chunkOverlap = chunkOverlap;
            this.fallbackLanguage = fallbackLanguage;
        }

        @Override
        public List<TextSegment> split(Document document) {
            String fileName = document.metadata().getString(fileNameMetadataKey); // adjust to your Metadata API if needed
            Language language = (fileName != null) ? Language.fromFileName(fileName) : fallbackLanguage;
            return new CodeDocumentSplitter(language, chunkSize, chunkOverlap).split(document);
        }
    }
}
