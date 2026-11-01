package be.anna.sixletterwords.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CombinationRulesTest {

    @Test
    void defaultsAreSixLettersWithoutPartLimit() {
        CombinationRules rules = CombinationRules.defaults();

        assertThat(rules.targetLength()).isEqualTo(6);
        assertThat(rules.maxParts()).isEqualTo(6);
    }

    @Test
    void withoutPartLimitAllowsAsManyPartsAsLetters() {
        assertThat(CombinationRules.withoutPartLimit(8).maxParts()).isEqualTo(8);
    }

    @Test
    void rejectsTargetLengthBelowTwo() {
        assertThatThrownBy(() -> new CombinationRules(1, 2))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsMaxPartsBelowTwo() {
        assertThatThrownBy(() -> new CombinationRules(6, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}