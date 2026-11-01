package be.anna.sixletterwords.port.out;


import be.anna.sixletterwords.domain.Combination;

import java.util.List;

public interface SaveCombinationsPort {

    void saveAll(List<Combination> combinations);
}