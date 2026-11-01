package be.anna.sixletterwords.adapter.out.persistence;

import be.anna.sixletterwords.domain.Combination;
import be.anna.sixletterwords.port.out.SaveCombinationsPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Component
class CombinationPersistenceAdapter implements SaveCombinationsPort {

    private static final String PART_SEPARATOR = "+";

    private final CombinationJpaRepository jpaRepository;

    CombinationPersistenceAdapter(CombinationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public void saveAll(List<Combination> combinations) {
        List<CombinationEntity> entities = combinations.stream()
                .map(CombinationPersistenceAdapter::toEntity)
                .toList();
        jpaRepository.saveAll(entities);
    }

    private static CombinationEntity toEntity(Combination combination) {
        return new CombinationEntity(String.join(PART_SEPARATOR, combination.parts()), combination.result());
    }
}


