package be.anna.sixletterwords.core;

import be.anna.sixletterwords.domain.BacktrackingCombinationFinder;
import be.anna.sixletterwords.domain.Combination;
import be.anna.sixletterwords.domain.CombinationRules;
import be.anna.sixletterwords.port.out.SaveCombinationsPort;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FindCombinationServiceTest {

    private final InMemoryCombinationRepository repository = new InMemoryCombinationRepository();
    private final FindCombinationService service = new FindCombinationService(new BacktrackingCombinationFinder(), repository);

    @Test
    void returnsTheCombinationsFound() {
        List<Combination> result = service.findCombinations(
                List.of("foobar", "fo", "obar"), CombinationRules.defaults());

        assertThat(result).containsExactly(Combination.of("fo", "obar"));
    }

    @Test
    void storesTheCombinationsFound() {
        List<Combination> result = service.findCombinations(
                List.of("foobar", "fo", "obar", "foo", "bar"), CombinationRules.defaults());

        assertThat(repository.saved).containsExactlyElementsOf(result);
    }

    @Test
    void storesNothingWhenNothingIsFound() {
        List<Combination> result = service.findCombinations(List.of("foobar", "xyz"), CombinationRules.defaults());

        assertThat(result).isEmpty();
        assertThat(repository.saved).isEmpty();
    }

    @Test
    void appliesTheGivenRules() {
        List<Combination> result = service.findCombinations(
                List.of("foobar", "f", "oo", "bar", "foo"), new CombinationRules(6, 2));

        assertThat(result).containsExactly(Combination.of("foo", "bar"));
    }

    @Test
    void rejectsMissingDependencies() {
        assertThatThrownBy(() -> new FindCombinationService(null, repository))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new FindCombinationService(new BacktrackingCombinationFinder(), null))
                .isInstanceOf(NullPointerException.class);
    }

    /** A hand-written fake: simpler to read than a mock, and checks real behaviour. */
    private static final class InMemoryCombinationRepository implements SaveCombinationsPort {

        private final List<Combination> saved = new ArrayList<>();

        @Override
        public void saveAll(List<Combination> combinations) {
            saved.addAll(combinations);
        }
    }
}

