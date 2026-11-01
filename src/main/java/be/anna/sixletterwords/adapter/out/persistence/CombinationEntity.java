package be.anna.sixletterwords.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "combination")
public class CombinationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** The parts joined with "+", e.g. "fo+obar". */
    @Column(nullable = false)
    private String parts;

    @Column(nullable = false)
    private String result;

    protected CombinationEntity() {
        // required by JPA
    }

    CombinationEntity(String parts, String result) {
        this.parts = parts;
        this.result = result;
    }

    public UUID getId() {
        return id;
    }

    public String getParts() {
        return parts;
    }

    public String getResult() {
        return result;
    }
}