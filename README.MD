# code-splitter

A code-aware `DocumentSplitter` for [langchain4j](https://github.com/langchain4j/langchain4j).

langchain4j ships `DocumentSplitters.recursive(...)`, but — unlike Python LangChain's
`RecursiveCharacterTextSplitter.from_language(...)` — it has no concept of *language-specific*
separators. This project ports that missing piece: a recursive splitter that knows Java,
Kotlin, Python, JS/TS, Go, C/C++/C#, Ruby, PHP, Rust, Swift, Markdown, and HTML each have
different "natural" chunk boundaries (`class`, `def`, `func`, `## heading`, `<div>`, ...),
and falls back progressively to paragraph → line → word → character splitting when a
structural chunk is still too big.

## Why

```python
# Python LangChain
RecursiveCharacterTextSplitter.from_language(language=Language.JAVA, chunk_size=600, chunk_overlap=30)
```

had no equivalent in langchain4j. This gives you:

```java
// Java, same idea
DocumentSplitter splitter = CodeSplitters.recursive(Language.JAVA, 600, 30);
```

## Project layout

```
src/main/java/com/codeanalyzer/splitter/
├── Language.java                       language -> separator table + file-extension detection
├── RecursiveCharacterTextSplitter.java  the algorithm itself (zero langchain4j dependency)
├── CodeDocumentSplitter.java           langchain4j DocumentSplitter adapter
└── CodeSplitters.java                  static factory methods (CodeSplitters.recursive(...))

src/test/java/com/codeanalyzer/splitter/
├── ManualTest.java                              plain `main()` smoke test, no deps required
└── RecursiveCharacterTextSplitterTest.java       JUnit 5 suite
```

`RecursiveCharacterTextSplitter` and `Language` have **no langchain4j import at all** — you
can copy just those two files if you ever need the algorithm outside langchain4j. The
`Code*` classes are the thin adapter layer that plugs it into `DocumentSplitter`.

## Install

Add to your `pom.xml` (this repo's `pom.xml` already has it):

```xml
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-core</artifactId>
    <version>0.35.0</version> <!-- match whatever your project already uses -->
</dependency>
```

Then copy `src/main/java/com/codeanalyzer/splitter/` into your codebase (or turn this into
a shared module — up to you).

## Usage

### Basic — fixed language

```java
import com.codeanalyzer.splitter.CodeSplitters;
import com.codeanalyzer.splitter.Language;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.segment.TextSegment;

DocumentSplitter splitter = CodeSplitters.recursive(Language.JAVA, 600, 30);

Document document = Document.from(javaSourceCode);
List<TextSegment> chunks = splitter.split(document);
```

### Wired into ingestion (RAG pipeline for your code analyzer agent)

```java
EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
        .documentSplitter(CodeSplitters.recursive(Language.JAVA, 600, 30))
        .embeddingModel(embeddingModel)
        .embeddingStore(embeddingStore)
        .build();

ingestor.ingest(document);
```

### Mixed-language repo — auto-detect per file

If your agent walks a whole repo (`.java`, `.py`, `.ts`, ...), tag each `Document`'s
metadata with its file name when you load it, then use the auto-detecting splitter:

```java
Document document = Document.document(sourceText, Metadata.from("file_name", "OrderService.java"));

DocumentSplitter splitter = CodeSplitters.recursiveAutoDetect(600, 30);
List<TextSegment> chunks = splitter.split(document); // detects Language.JAVA from the extension
```

`Language.fromFileName(...)` is also public if you want to do the lookup yourself.

## Supported languages

`JAVA`, `KOTLIN`, `SCALA`, `PYTHON`, `JS`, `TS`, `GO`, `C`, `CPP`, `CSHARP`, `RUBY`, `PHP`,
`RUST`, `SWIFT`, `MARKDOWN`, `HTML`, plus `TEXT` as a plain paragraph/line/word fallback.
Adding another language is a ~15-line addition to `Language.java` — just supply an ordered
separator list from coarsest to finest.

## How the splitting actually works

1. Try the language's separators in order (e.g. Java: `\nclass `, `\ninterface `, `\npublic `,
   `\nprivate `, `\nif `, `\nfor `, ... down to `\n\n`, `\n`, `" "`, `""`).
2. Pick the **first** separator that actually occurs in the text.
3. Split on it. Any resulting piece still bigger than `chunkSize` gets recursively re-split
   with the *next* (finer) separator.
4. Pieces that fit are greedily merged back together up to `chunkSize`, sliding a
   `chunkOverlap`-sized trailing window into the next chunk so consecutive chunks share
   boundary context.

This is a direct port of the algorithm behind Python LangChain's
`RecursiveCharacterTextSplitter`, just with `Language` → separator tables re-expressed in
Java and wired into langchain4j's `DocumentSplitter` interface instead of Python's
`TextSplitter` base class.

## Build & test

```bash
mvn test
```

`ManualTest.main()` also runs standalone with nothing but the JDK, if you want to sanity
check the algorithm without pulling in Maven/langchain4j at all:

```bash
javac -d out src/main/java/com/codeanalyzer/splitter/Language.java \
             src/main/java/com/codeanalyzer/splitter/RecursiveCharacterTextSplitter.java \
             src/test/java/com/codeanalyzer/splitter/ManualTest.java
java -cp out com.codeanalyzer.splitter.ManualTest
```

## A note on the `Metadata` API

`CodeDocumentSplitter` calls `document.metadata().copy()` and `metadata.put(key, value)`,
which match langchain4j-core's `Metadata` class in the 0.3x/1.x line. If your pinned
version's `Metadata` shape differs, the four lines that touch it in
`CodeDocumentSplitter.split(...)` are the only ones you'll need to tweak — everything else
in the package has no langchain4j dependency and won't be affected.
