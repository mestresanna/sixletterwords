package be.anna.sixletterwords.port.in;

import be.anna.sixletterwords.domain.Combination;
import be.anna.sixletterwords.domain.CombinationRules;

import java.util.Collection;
import java.util.List;

public interface FindCombinationUseCase {
    List<Combination> findCombinations(Collection<String> words, CombinationRules rules);
}
