package com.gamegrindSplitter.splitter;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Programming / markup languages supported by {@link RecursiveCharacterTextSplitter}.
 * <p>
 * Each language carries an ordered list of separators, from "coarsest" (top-level
 * structural boundaries like class/function definitions) down to the finest
 * (single characters). This mirrors the separator tables used by LangChain's
 * Python {@code RecursiveCharacterTextSplitter.from_language(...)}, so chunk
 * boundaries land on syntactically meaningful lines instead of mid-token.
 * <p>
 * The splitter always tries separators in order and only falls back to a finer
 * one when a chunk produced by a coarser separator is still too big.
 */
public enum Language {

    JAVA(
            "\nclass ",
            "\ninterface ",
            "\nenum ",
            "\npublic ",
            "\nprotected ",
            "\nprivate ",
            "\nstatic ",
            "\nif ",
            "\nfor ",
            "\nwhile ",
            "\nswitch ",
            "\ncase ",
            "\ntry ",
            "\ncatch ",
            "\nfinally ",
            "\n\n",
            "\n",
            " ",
            ""
    ),

    KOTLIN(
            "\nclass ",
            "\ninterface ",
            "\nobject ",
            "\nfun ",
            "\nval ",
            "\nvar ",
            "\nif ",
            "\nfor ",
            "\nwhile ",
            "\nwhen ",
            "\ntry ",
            "\ncatch ",
            "\nfinally ",
            "\n\n",
            "\n",
            " ",
            ""
    ),

    SCALA(
            "\nclass ",
            "\nobject ",
            "\ntrait ",
            "\ndef ",
            "\nval ",
            "\nvar ",
            "\nif ",
            "\nfor ",
            "\nwhile ",
            "\nmatch ",
            "\ncase ",
            "\n\n",
            "\n",
            " ",
            ""
    ),

    PYTHON(
            "\nclass ",
            "\ndef ",
            "\n\tdef ",
            "\nif ",
            "\nfor ",
            "\nwhile ",
            "\ntry ",
            "\nexcept ",
            "\nwith ",
            "\n\n",
            "\n",
            " ",
            ""
    ),

    JS(
            "\nfunction ",
            "\nasync function ",
            "\nconst ",
            "\nlet ",
            "\nvar ",
            "\nclass ",
            "\nif ",
            "\nfor ",
            "\nwhile ",
            "\nswitch ",
            "\ncase ",
            "\ndefault ",
            "\n\n",
            "\n",
            " ",
            ""
    ),

    TS(
            "\ninterface ",
            "\ntype ",
            "\nenum ",
            "\nfunction ",
            "\nasync function ",
            "\nconst ",
            "\nlet ",
            "\nvar ",
            "\nclass ",
            "\nif ",
            "\nfor ",
            "\nwhile ",
            "\nswitch ",
            "\ncase ",
            "\ndefault ",
            "\n\n",
            "\n",
            " ",
            ""
    ),

    GO(
            "\nfunc ",
            "\nvar ",
            "\nconst ",
            "\ntype ",
            "\nif ",
            "\nfor ",
            "\nswitch ",
            "\ncase ",
            "\n\n",
            "\n",
            " ",
            ""
    ),

    C(
            "\nvoid ",
            "\nint ",
            "\nfloat ",
            "\ndouble ",
            "\nchar ",
            "\nstruct ",
            "\nif ",
            "\nfor ",
            "\nwhile ",
            "\nswitch ",
            "\ncase ",
            "\n\n",
            "\n",
            " ",
            ""
    ),

    CPP(
            "\nclass ",
            "\nnamespace ",
            "\ntemplate ",
            "\nvoid ",
            "\nint ",
            "\nfloat ",
            "\ndouble ",
            "\nstruct ",
            "\nif ",
            "\nfor ",
            "\nwhile ",
            "\nswitch ",
            "\ncase ",
            "\n\n",
            "\n",
            " ",
            ""
    ),

    CSHARP(
            "\ninterface ",
            "\nenum ",
            "\nclass ",
            "\npublic ",
            "\nprotected ",
            "\nprivate ",
            "\nstatic ",
            "\nreturn ",
            "\nif ",
            "\ncontinue ",
            "\nfor ",
            "\nforeach ",
            "\nwhile ",
            "\nswitch ",
            "\nbreak ",
            "\ncase ",
            "\nelse ",
            "\ntry ",
            "\nthrow ",
            "\nfinally ",
            "\ncatch ",
            "\n\n",
            "\n",
            " ",
            ""
    ),

    RUBY(
            "\nclass ",
            "\nmodule ",
            "\ndef ",
            "\nif ",
            "\nunless ",
            "\nwhile ",
            "\nfor ",
            "\ndo ",
            "\nbegin ",
            "\nrescue ",
            "\n\n",
            "\n",
            " ",
            ""
    ),

    PHP(
            "\nfunction ",
            "\nclass ",
            "\nif ",
            "\nforeach ",
            "\nwhile ",
            "\ndo ",
            "\nswitch ",
            "\ncase ",
            "\n\n",
            "\n",
            " ",
            ""
    ),

    RUST(
            "\nfn ",
            "\nstruct ",
            "\nenum ",
            "\nimpl ",
            "\ntrait ",
            "\npub ",
            "\nif ",
            "\nfor ",
            "\nwhile ",
            "\nmatch ",
            "\nloop ",
            "\n\n",
            "\n",
            " ",
            ""
    ),

    SWIFT(
            "\nfunc ",
            "\nclass ",
            "\nstruct ",
            "\nenum ",
            "\nprotocol ",
            "\nextension ",
            "\nif ",
            "\nfor ",
            "\nwhile ",
            "\nswitch ",
            "\ncase ",
            "\n\n",
            "\n",
            " ",
            ""
    ),

    MARKDOWN(
            "\n# ",
            "\n## ",
            "\n### ",
            "\n#### ",
            "\n#####",
            "\n######",
            "\n```\n",
            "\n\n***\n\n",
            "\n\n---\n\n",
            "\n\n___\n\n",
            "\n\n",
            "\n",
            " ",
            ""
    ),

    HTML(
            "<body",
            "<div",
            "<p",
            "<br",
            "<li",
            "<h1",
            "<h2",
            "<h3",
            "<h4",
            "<h5",
            "<h6",
            "<span",
            "<table",
            "<tr",
            "<td",
            "<th",
            "<ul",
            "<ol",
            "<header",
            "<footer",
            "<nav",
            "<head",
            "<style",
            "<script",
            "<!--",
            " ",
            ""
    ),

    /** Plain-text fallback: paragraph / line / word / character, no code-aware separators. */
    TEXT(
            "\n\n",
            "\n",
            " ",
            ""
    );

    private final List<String> separators;

    Language(String... separators) {
        this.separators = Arrays.asList(separators);
    }

    /** Ordered separators, coarsest first, always ending in "" (character-level fallback). */
    public List<String> getSeparators() {
        return separators;
    }

    /**
     * Best-effort language detection from a file name / path based on its extension.
     * Falls back to {@link #TEXT} for anything unrecognized.
     */
    public static Language fromFileName(String fileName) {
        if (fileName == null) {
            return TEXT;
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return TEXT;
        }
        String ext = fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        switch (ext) {
            case "java":
                return JAVA;
            case "kt":
            case "kts":
                return KOTLIN;
            case "scala":
                return SCALA;
            case "py":
                return PYTHON;
            case "js":
            case "jsx":
            case "mjs":
                return JS;
            case "ts":
            case "tsx":
                return TS;
            case "go":
                return GO;
            case "c":
            case "h":
                return C;
            case "cpp":
            case "cc":
            case "cxx":
            case "hpp":
                return CPP;
            case "cs":
                return CSHARP;
            case "rb":
                return RUBY;
            case "php":
                return PHP;
            case "rs":
                return RUST;
            case "swift":
                return SWIFT;
            case "md":
            case "markdown":
                return MARKDOWN;
            case "html":
            case "htm":
                return HTML;
            default:
                return TEXT;
        }
    }
}
