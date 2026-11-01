package be.anna.sixletterwords.domain;
import java.util.List;
import java.util.Objects;

public record Combination(List<String> parts) {

    public static final int MIN_PARTS = 2;

    public Combination {
        Objects.requireNonNull(parts, "parts must not be null");
        if (parts.size() < MIN_PARTS) {
            throw new IllegalArgumentException(
                    "A combination needs at least " + MIN_PARTS + " parts, got " + parts.size());
        }
        if (parts.stream().anyMatch(part -> part == null || part.isEmpty())) {
            throw new IllegalArgumentException("Parts must not be null or empty: " + parts);
        }
        parts = List.copyOf(parts);
    }

    public static Combination of(String... parts) {
        return new Combination(List.of(parts));
    }

    public String result() {
        return String.join("", parts);
    }
}
