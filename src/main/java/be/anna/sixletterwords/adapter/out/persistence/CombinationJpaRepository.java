package be.anna.sixletterwords.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface CombinationJpaRepository extends JpaRepository<CombinationEntity, UUID> {
}
