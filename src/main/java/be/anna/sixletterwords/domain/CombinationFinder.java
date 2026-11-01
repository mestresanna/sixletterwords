package be.anna.sixletterwords.domain;

import java.util.Collection;
import java.util.List;


public interface CombinationFinder {
    List<Combination> find(
            Collection<String> words,
            CombinationRules rules
    );

}