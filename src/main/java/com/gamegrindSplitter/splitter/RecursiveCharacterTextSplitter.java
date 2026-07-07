package com.gamegrindSplitter.splitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Java port of LangChain's {@code RecursiveCharacterTextSplitter}, the same algorithm
 * that backs Python's {@code RecursiveCharacterTextSplitter.from_language(...)}.
 * <p>
 * Strategy: try the coarsest separator for the given language first (e.g. "\nclass ").
 * Any resulting piece that still exceeds {@code chunkSize} is recursively re-split using
 * the next, finer separator (e.g. "\npublic ", then "\n\n", then "\n", then " ", then "").
 * Pieces that fit are then greedily merged back together up to {@code chunkSize},
 * sliding a {@code chunkOverlap}-sized window of trailing context into the next chunk.
 * <p>
 * This class has zero dependency on langchain4j — it's plain Java and unit-testable
 * on its own. {@link CodeDocumentSplitter} is the thin adapter that plugs it into
 * langchain4j's {@code DocumentSplitter} interface.
 */
public class RecursiveCharacterTextSplitter {

    private final List<String> separators;
    private final int chunkSize;
    private final int chunkOverlap;
    private final boolean keepSeparator;

    public RecursiveCharacterTextSplitter(List<String> separators, int chunkSize, int chunkOverlap) {
        this(separators, chunkSize, chunkOverlap, true);
    }

    public RecursiveCharacterTextSplitter(List<String> separators, int chunkSize, int chunkOverlap, boolean keepSeparator) {
        if (chunkOverlap >= chunkSize) {
            throw new IllegalArgumentException(
                    "chunkOverlap (" + chunkOverlap + ") must be smaller than chunkSize (" + chunkSize + ")");
        }
        this.separators = separators;
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
        this.keepSeparator = keepSeparator;
    }

    /** Convenience factory: build a splitter tuned for a given {@link Language}. */
    public static RecursiveCharacterTextSplitter fromLanguage(Language language, int chunkSize, int chunkOverlap) {
        return new RecursiveCharacterTextSplitter(language.getSeparators(), chunkSize, chunkOverlap, true);
    }

    /** Split raw text into chunks, each roughly {@code chunkSize} characters or smaller. */
    public List<String> splitText(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }
        return splitTextRecursive(text, separators);
    }

    private List<String> splitTextRecursive(String text, List<String> remainingSeparators) {
        List<String> finalChunks = new ArrayList<>();

        // Pick the first separator (in priority order) that actually occurs in this text.
        // Everything after it becomes the fallback chain if a piece is still too big.
        String separator = remainingSeparators.get(remainingSeparators.size() - 1);
        List<String> nextSeparators = Collections.emptyList();
        for (int i = 0; i < remainingSeparators.size(); i++) {
            String candidate = remainingSeparators.get(i);
            if (candidate.isEmpty()) {
                separator = candidate;
                break;
            }
            if (text.contains(candidate)) {
                separator = candidate;
                nextSeparators = remainingSeparators.subList(i + 1, remainingSeparators.size());
                break;
            }
        }

        List<String> splits = splitBySeparator(text, separator);

        List<String> goodSplits = new ArrayList<>();
        String mergeSeparator = keepSeparator ? "" : separator;

        for (String piece : splits) {
            if (piece.length() < chunkSize) {
                goodSplits.add(piece);
                continue;
            }
            if (!goodSplits.isEmpty()) {
                finalChunks.addAll(mergeSplits(goodSplits, mergeSeparator));
                goodSplits = new ArrayList<>();
            }
            if (nextSeparators.isEmpty()) {
                // No finer separator left (we're already at "") - accept the oversized piece as-is.
                finalChunks.add(piece);
            } else {
                finalChunks.addAll(splitTextRecursive(piece, nextSeparators));
            }
        }
        if (!goodSplits.isEmpty()) {
            finalChunks.addAll(mergeSplits(goodSplits, mergeSeparator));
        }
        return finalChunks;
    }

    /**
     * Split {@code text} on every literal occurrence of {@code separator}. When
     * {@code keepSeparator} is true, the separator is reattached to the front of the
     * piece that follows it (so e.g. "\nclass " stays glued to the class body it
     * introduces, instead of being discarded).
     */
    private List<String> splitBySeparator(String text, String separator) {
        if (separator.isEmpty()) {
            List<String> chars = new ArrayList<>(text.length());
            for (int i = 0; i < text.length(); i++) {
                chars.add(String.valueOf(text.charAt(i)));
            }
            return chars;
        }

        List<String> rawParts = new ArrayList<>();
        int sepLen = separator.length();
        int start = 0;
        int idx;
        while ((idx = text.indexOf(separator, start)) != -1) {
            rawParts.add(text.substring(start, idx));
            start = idx + sepLen;
        }
        rawParts.add(text.substring(start));

        List<String> result = new ArrayList<>();
        if (!keepSeparator) {
            for (String part : rawParts) {
                if (!part.isEmpty()) {
                    result.add(part);
                }
            }
            return result;
        }

        for (int i = 0; i < rawParts.size(); i++) {
            String part = (i == 0) ? rawParts.get(0) : separator + rawParts.get(i);
            if (!part.isEmpty()) {
                result.add(part);
            }
        }
        return result;
    }

    /**
     * Greedily pack small pieces into chunks up to {@code chunkSize}, carrying trailing
     * pieces worth up to {@code chunkOverlap} characters over into the next chunk so
     * consecutive chunks share context at the boundary.
     */
    private List<String> mergeSplits(List<String> splits, String separator) {
        List<String> docs = new ArrayList<>();
        List<String> currentDoc = new ArrayList<>();
        int total = 0;
        int sepLen = separator.length();

        for (String piece : splits) {
            int pieceLen = piece.length();
            int projected = total + pieceLen + (currentDoc.isEmpty() ? 0 : sepLen);

            if (projected > chunkSize && !currentDoc.isEmpty()) {
                docs.add(String.join(separator, currentDoc));

                // Slide the window: drop leading pieces until we're back under the
                // overlap budget (or dropping further would empty the window / we've
                // made room for the new piece).
                while (!currentDoc.isEmpty()
                        && (total > chunkOverlap
                            || (total + pieceLen + (currentDoc.size() > 1 ? sepLen : 0) > chunkSize && total > 0))) {
                    total -= currentDoc.get(0).length() + (currentDoc.size() > 1 ? sepLen : 0);
                    currentDoc.remove(0);
                }
            }

            currentDoc.add(piece);
            total += pieceLen + (currentDoc.size() > 1 ? sepLen : 0);
        }

        if (!currentDoc.isEmpty()) {
            docs.add(String.join(separator, currentDoc));
        }
        return docs;
    }
}
