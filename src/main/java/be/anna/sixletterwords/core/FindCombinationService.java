package be.anna.sixletterwords.core;

import be.anna.sixletterwords.port.in.FindCombinationUseCase;
import be.anna.sixletterwords.port.out.SaveCombinationsPort;
import be.anna.sixletterwords.domain.Combination;
import be.anna.sixletterwords.domain.CombinationFinder;
import be.anna.sixletterwords.domain.CombinationRules;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Service
public class FindCombinationService implements FindCombinationUseCase {

    private final CombinationFinder finder;
    private final SaveCombinationsPort saveCombinationsPort;

    public FindCombinationService(CombinationFinder finder, SaveCombinationsPort saveCombinationsPort) {
        this.finder = Objects.requireNonNull(finder, "finder must not be null");
        this.saveCombinationsPort = Objects.requireNonNull(saveCombinationsPort, "repository must not be null");
    }

    @Override
    public List<Combination> findCombinations(Collection<String> words, CombinationRules rules) {
        List<Combination> combinations = finder.find(words, rules);
        saveCombinationsPort.saveAll(combinations);
        return combinations;
    }
}

