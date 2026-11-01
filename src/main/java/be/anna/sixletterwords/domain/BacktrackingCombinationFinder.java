package be.anna.sixletterwords.domain;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Objects;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class BacktrackingCombinationFinder implements CombinationFinder {
    private static final Comparator<Combination> OUTPUT_ORDER =
            Comparator.comparing(Combination::result)
                    .thenComparing(combination -> String.join("+", combination.parts()));

    @Override
    public List<Combination> find(Collection<String> words, CombinationRules rules) {
        Objects.requireNonNull(words, "words must not be null");
        Objects.requireNonNull(rules, "rules must not be null");

        Set<String> dictionary = toDictionary(words);

        return dictionary.stream()
                .filter(word -> word.length() == rules.targetLength())
                .flatMap(target -> new SplitSearch(target, dictionary, rules.maxParts()).run().stream())
                .sorted(OUTPUT_ORDER)
                .toList();
    }

    private static Set<String> toDictionary(Collection<String> words) {
        return words.stream()
                .filter(word -> word != null && !word.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }

    private static final class SplitSearch {

        private final String target;
        private final Set<String> dictionary;
        private final int maxParts;
        private final List<String> currentParts = new ArrayList<>();
        private final List<Combination> found = new ArrayList<>();

        private SplitSearch(String target, Set<String> dictionary, int maxParts) {
            this.target = target;
            this.dictionary = dictionary;
            this.maxParts = maxParts;
        }

        private List<Combination> run() {
            searchFrom(0);
            return found;
        }

        private void searchFrom(int start) {
            if (start == target.length()) {
                recordIfValid();
                return;
            }
            if (currentParts.size() >= maxParts) {
                return;
            }
            for (int end = start + 1; end <= target.length(); end++) {
                String part = target.substring(start, end);
                if (dictionary.contains(part)) {
                    currentParts.add(part);
                    searchFrom(end);
                    currentParts.remove(currentParts.size() - 1);
                }
            }
        }

        private void recordIfValid() {
            if (currentParts.size() >= Combination.MIN_PARTS) {
                found.add(new Combination(currentParts));
            }
        }
    }
}
