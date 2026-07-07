package com.codeanalyzer.splitter;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecursiveCharacterTextSplitterTest {

    private static final String JAVA_SOURCE =
            "package com.example.orders;\n\n" +
            "public class OrderService {\n\n" +
            "    private final OrderRepository repository;\n\n" +
            "    public OrderService(OrderRepository repository) {\n" +
            "        this.repository = repository;\n" +
            "    }\n\n" +
            "    public Order placeOrder(OrderRequest request) {\n" +
            "        validate(request);\n" +
            "        return repository.save(new Order(request));\n" +
            "    }\n\n" +
            "    private void validate(OrderRequest request) {\n" +
            "        if (request.getCustomerId() == null) {\n" +
            "            throw new IllegalArgumentException(\"customerId required\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n";

    @Test
    void everyChunkRespectsTheSizeLimit() {
        int chunkSize = 200;
        RecursiveCharacterTextSplitter splitter =
                RecursiveCharacterTextSplitter.fromLanguage(Language.JAVA, chunkSize, 20);

        List<String> chunks = splitter.splitText(JAVA_SOURCE);

        assertFalse(chunks.isEmpty());
        for (String chunk : chunks) {
            assertTrue(chunk.length() <= chunkSize,
                    "chunk exceeded chunkSize: " + chunk.length() + " > " + chunkSize);
        }
    }

    @Test
    void splitsLandOnJavaStructuralBoundaries() {
        RecursiveCharacterTextSplitter splitter =
                RecursiveCharacterTextSplitter.fromLanguage(Language.JAVA, 300, 30);

        List<String> chunks = splitter.splitText(JAVA_SOURCE);

        boolean hasClassBoundary = chunks.stream().anyMatch(c -> c.contains("class OrderService"));
        boolean hasMethodBoundary = chunks.stream().anyMatch(c -> c.contains("public Order placeOrder"));

        assertTrue(hasClassBoundary, "expected a chunk starting at the class declaration");
        assertTrue(hasMethodBoundary, "expected a chunk starting at a method declaration");
    }

    @Test
    void largerChunkSizeProducesFewerChunks() {
        RecursiveCharacterTextSplitter small =
                RecursiveCharacterTextSplitter.fromLanguage(Language.JAVA, 150, 15);
        RecursiveCharacterTextSplitter large =
                RecursiveCharacterTextSplitter.fromLanguage(Language.JAVA, 600, 30);

        int smallChunks = small.splitText(JAVA_SOURCE).size();
        int largeChunks = large.splitText(JAVA_SOURCE).size();

        assertTrue(largeChunks <= smallChunks);
    }

    @Test
    void rejectsOverlapNotSmallerThanChunkSize() {
        assertThrows(IllegalArgumentException.class,
                () -> new RecursiveCharacterTextSplitter(Language.JAVA.getSeparators(), 100, 100));
        assertThrows(IllegalArgumentException.class,
                () -> new RecursiveCharacterTextSplitter(Language.JAVA.getSeparators(), 100, 150));
    }

    @Test
    void emptyAndNullTextProduceNoChunks() {
        RecursiveCharacterTextSplitter splitter =
                RecursiveCharacterTextSplitter.fromLanguage(Language.JAVA, 300, 30);

        assertTrue(splitter.splitText("").isEmpty());
        assertTrue(splitter.splitText(null).isEmpty());
    }

    @Test
    void languageIsDetectedFromFileExtension() {
        assertEquals(Language.JAVA, Language.fromFileName("OrderService.java"));
        assertEquals(Language.PYTHON, Language.fromFileName("model.py"));
        assertEquals(Language.TS, Language.fromFileName("app.tsx"));
        assertEquals(Language.TEXT, Language.fromFileName("README"));
        assertEquals(Language.TEXT, Language.fromFileName("data.unknownext"));
    }
}
