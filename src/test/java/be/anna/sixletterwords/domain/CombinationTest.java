package be.anna.sixletterwords.domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CombinationTest {

    @Test
    void resultIsTheConcatenationOfItsParts() {
        assertThat(Combination.of("fo", "obar").result()).isEqualTo("foobar");
    }

    @Test
    void supportsMoreThanTwoParts() {
        assertThat(Combination.of("f", "oo", "bar").result()).isEqualTo("foobar");
    }

    @Test
    void combinationsWithSamePartsAreEqual() {
        assertThat(Combination.of("fo", "obar")).isEqualTo(Combination.of("fo", "obar"));
    }

    @Test
    void sameResultWithDifferentPartsIsADifferentCombination() {
        assertThat(Combination.of("fo", "obar")).isNotEqualTo(Combination.of("foo", "bar"));
    }

    @Test
    void rejectsFewerThanTwoParts() {
        assertThatThrownBy(() -> Combination.of("foobar"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsEmptyParts() {
        assertThatThrownBy(() -> Combination.of("foobar", ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNullParts() {
        assertThatThrownBy(() -> new Combination(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Combination(Arrays.asList("foo", null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void isNotAffectedByChangesToTheOriginalList() {
        List<String> parts = new ArrayList<>(List.of("fo", "obar"));
        Combination combination = new Combination(parts);

        parts.add("x");

        assertThat(combination.parts()).containsExactly("fo", "obar");
    }
}