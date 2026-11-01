package be.anna.sixletterwords.domain;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BacktrackingCombinationFinderTest {

    private final CombinationFinder finder = new BacktrackingCombinationFinder();

    @Test
    void findsTheExampleFromTheBrief() {
        List<Combination> result = finder.find(List.of("foobar", "fo", "obar"), CombinationRules.defaults());

        assertThat(result).containsExactly(Combination.of("fo", "obar"));
    }

    @Test
    void findsNothingWhenNoWordsFitTogether() {
        List<Combination> result = finder.find(List.of("foobar", "xy", "zzzz"), CombinationRules.defaults());

        assertThat(result).isEmpty();
    }

    @Test
    void findsNothingWhenTheTargetWordIsMissing() {
        List<Combination> result = finder.find(List.of("fo", "obar"), CombinationRules.defaults());

        assertThat(result).isEmpty();
    }

    @Test
    void findsEverySplitOfTheSameWord() {
        List<Combination> result = finder.find(
                List.of("foobar", "fo", "obar", "foo", "bar"), CombinationRules.defaults());

        assertThat(result).containsExactly(
                Combination.of("fo", "obar"),
                Combination.of("foo", "bar"));
    }

    @Test
    void findsCombinationsOfMoreThanTwoParts() {
        List<Combination> result = finder.find(List.of("foobar", "f", "oo", "bar"), CombinationRules.defaults());

        assertThat(result).containsExactly(Combination.of("f", "oo", "bar"));
    }

    @Test
    void respectsTheMaximumNumberOfParts() {
        List<String> words = List.of("foobar", "f", "oo", "bar", "foo");

        List<Combination> result = finder.find(words, new CombinationRules(6, 2));

        assertThat(result).containsExactly(Combination.of("foo", "bar"));
    }

    @Test
    void supportsADifferentTargetLength() {
        List<Combination> result = finder.find(
                List.of("foobar", "fo", "obar", "abcd", "ab", "cd"), CombinationRules.withoutPartLimit(4));

        assertThat(result).containsExactly(Combination.of("ab", "cd"));
    }

    @Test
    void allowsTheSameWordTwiceInOneCombination() {
        List<Combination> result = finder.find(List.of("foofoo", "foo"), CombinationRules.defaults());

        assertThat(result).containsExactly(Combination.of("foo", "foo"));
    }

    @Test
    void doesNotReportAWordAsACombinationOfItself() {
        List<Combination> result = finder.find(List.of("foobar"), CombinationRules.defaults());

        assertThat(result).isEmpty();
    }

    @Test
    void ignoresDuplicateInputWords() {
        List<Combination> result = finder.find(
                List.of("foobar", "foobar", "fo", "fo", "obar"), CombinationRules.defaults());

        assertThat(result).containsExactly(Combination.of("fo", "obar"));
    }

    @Test
    void skipsBlankAndNullEntries() {
        List<Combination> result = finder.find(
                Arrays.asList("foobar", "", "  ", null, "fo", "obar"), CombinationRules.defaults());

        assertThat(result).containsExactly(Combination.of("fo", "obar"));
    }

    @Test
    void returnsResultsSortedByWordThenParts() {
        List<Combination> result = finder.find(
                List.of("zzzzzz", "zzz", "foobar", "foo", "bar", "fo", "obar"), CombinationRules.defaults());

        assertThat(result).containsExactly(
                Combination.of("fo", "obar"),
                Combination.of("foo", "bar"),
                Combination.of("zzz", "zzz"));
    }

    @Test
    void rejectsNullArguments() {
        assertThatThrownBy(() -> finder.find(null, CombinationRules.defaults()))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> finder.find(List.of("foobar"), null))
                .isInstanceOf(NullPointerException.class);
    }
}