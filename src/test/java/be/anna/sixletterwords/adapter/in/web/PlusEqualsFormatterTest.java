package be.anna.sixletterwords.adapter.in.web;

import be.anna.sixletterwords.domain.Combination;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlusEqualsFormatterTest {

    private final PlusEqualsFormatter formatter = new PlusEqualsFormatter();

    @Test
    void formatsTwoParts() {
        assertThat(formatter.format(Combination.of("fo", "obar"))).isEqualTo("fo+obar=foobar");
    }

    @Test
    void formatsMoreThanTwoParts() {
        assertThat(formatter.format(Combination.of("f", "oo", "bar"))).isEqualTo("f+oo+bar=foobar");
    }
}

