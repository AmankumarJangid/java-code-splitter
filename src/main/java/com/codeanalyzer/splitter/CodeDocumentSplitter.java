package com.codeanalyzer.splitter;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * langchain4j {@link DocumentSplitter} backed by {@link RecursiveCharacterTextSplitter}.
 * This is the piece that makes the plain-Java algorithm "just work" anywhere a
 * langchain4j {@code DocumentSplitter} is expected — ingestion pipelines,
 * {@code EmbeddingStoreIngestor}, etc.
 *
 * <pre>{@code
 * DocumentSplitter splitter = new CodeDocumentSplitter(Language.JAVA, 600, 30);
 *
 * EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
 *         .documentSplitter(splitter)
 *         .embeddingModel(embeddingModel)
 *         .embeddingStore(embeddingStore)
 *         .build();
 *
 * ingestor.ingest(document);
 * }</pre>
 *
 * NOTE ON METADATA API: this was written against the {@code Metadata} shape used by
 * langchain4j-core in the 0.3x / 1.x line ({@code metadata.copy()}, {@code metadata.put(key, value)}).
 * If your version's {@code Metadata} class differs slightly (some older versions only
 * accept String values, some expose {@code toMap()} instead of {@code copy()}), adjust
 * the four lines in {@link #split(Document)} that touch {@code metadata} accordingly —
 * everything else in this file is dependency-free logic.
 */
public class CodeDocumentSplitter implements DocumentSplitter {

    private final RecursiveCharacterTextSplitter splitter;
    private final Language language;

    public CodeDocumentSplitter(Language language, int chunkSize, int chunkOverlap) {
        this.language = Objects.requireNonNull(language, "language");
        this.splitter = RecursiveCharacterTextSplitter.fromLanguage(language, chunkSize, chunkOverlap);
    }

    /** Escape hatch: supply a pre-built splitter (e.g. custom separators) instead of a stock {@link Language} profile. */
    public CodeDocumentSplitter(RecursiveCharacterTextSplitter splitter, Language language) {
        this.splitter = Objects.requireNonNull(splitter, "splitter");
        this.language = Objects.requireNonNull(language, "language");
    }

    @Override
    public List<TextSegment> split(Document document) {
        List<String> chunks = splitter.splitText(document.text());

        List<TextSegment> segments = new ArrayList<>(chunks.size());
        int chunkIndex = 0;
        for (String chunk : chunks) {
            if (chunk.trim().isEmpty()) {
                continue;
            }

            Metadata metadata = document.metadata().copy();          // <- adjust if your version lacks copy()
            metadata.put("language", language.name());                // <- Metadata#put(String, Object) in recent versions
            metadata.put("chunk_index", String.valueOf(chunkIndex));
            metadata.put("chunk_count_hint", String.valueOf(chunks.size()));

            segments.add(TextSegment.from(chunk, metadata));
            chunkIndex++;
        }
        return segments;
    }
}
