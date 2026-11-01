package be.anna.sixletterwords.adapter.in.web;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WordListParserTest {

    private final WordListParser parser = new WordListParser();

    @Test
    void splitsOneWordPerLine() {
        assertThat(parser.parse("foobar\nfo\nobar")).containsExactly("foobar", "fo", "obar");
    }

    @Test
    void handlesWindowsLineEndings() {
        assertThat(parser.parse("foobar\r\nfo\r\nobar\r\n")).containsExactly("foobar", "fo", "obar");
    }

    @Test
    void handlesOldMacLineEndings() {
        assertThat(parser.parse("foobar\rfo\robar")).containsExactly("foobar", "fo", "obar");
    }

    @Test
    void skipsBlankLines() {
        assertThat(parser.parse("foobar\n\n\nfo\n   \nobar\n")).containsExactly("foobar", "fo", "obar");
    }

    @Test
    void trimsSpacesAndTabsAroundWords() {
        assertThat(parser.parse("  foobar  \n\tfo\t\n obar")).containsExactly("foobar", "fo", "obar");
    }

    @Test
    void treatsSpacesInsideALineAsSeparators() {
        assertThat(parser.parse("foobar fo obar")).containsExactly("foobar", "fo", "obar");
    }

    @Test
    void lowercasesWords() {
        assertThat(parser.parse("FooBar\nFO\nobar")).containsExactly("foobar", "fo", "obar");
    }

    @Test
    void keepsDuplicates() {
        assertThat(parser.parse("fo\nfo")).containsExactly("fo", "fo");
    }

    @Test
    void returnsEmptyListForEmptyBody() {
        assertThat(parser.parse("")).isEmpty();
    }

    @Test
    void returnsEmptyListForBlankBody() {
        assertThat(parser.parse(" \n\r\n\t ")).isEmpty();
    }

    @Test
    void returnsEmptyListForNullBody() {
        assertThat(parser.parse(null)).isEmpty();
    }
}