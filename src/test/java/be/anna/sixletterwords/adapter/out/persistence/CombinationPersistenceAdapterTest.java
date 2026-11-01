package be.anna.sixletterwords.adapter.out.persistence;

import be.anna.sixletterwords.domain.Combination;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@DataJpaTest
@Import(CombinationPersistenceAdapter.class)
class CombinationPersistenceAdapterTest {

    @Autowired
    private CombinationPersistenceAdapter adapter;

    @Autowired
    private CombinationJpaRepository jpaRepository;

    @Test
    void savesEveryCombination() {
        adapter.saveAll(List.of(Combination.of("fo", "obar"), Combination.of("foo", "bar")));

        assertThat(jpaRepository.count()).isEqualTo(2);
    }

    @Test
    void storesThePartsAndTheResultingWord() {
        adapter.saveAll(List.of(Combination.of("fo", "obar"), Combination.of("f", "oo", "bar")));

        assertThat(jpaRepository.findAll())
                .extracting(CombinationEntity::getParts, CombinationEntity::getResult)
                .containsExactlyInAnyOrder(
                        tuple("fo+obar", "foobar"),
                        tuple("f+oo+bar", "foobar"));
    }

    @Test
    void generatesAUniqueIdForEveryRow() {
        adapter.saveAll(List.of(Combination.of("fo", "obar"), Combination.of("foo", "bar")));

        List<UUID> ids = jpaRepository.findAll().stream().map(CombinationEntity::getId).toList();

        assertThat(ids).doesNotContainNull().doesNotHaveDuplicates();
    }

    @Test
    void savesNothingForAnEmptyList() {
        adapter.saveAll(List.of());

        assertThat(jpaRepository.count()).isZero();
    }
}