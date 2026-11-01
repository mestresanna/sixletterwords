package be.anna.sixletterwords.domain;

public record CombinationRules(int targetLength, int maxParts) {

    public static final int DEFAULT_TARGET_LENGTH = 6;

    public CombinationRules {
        if (targetLength < Combination.MIN_PARTS) {
            throw new IllegalArgumentException(
                    "targetLength must be at least " + Combination.MIN_PARTS + ", got " + targetLength);
        }
        if (maxParts < Combination.MIN_PARTS) {
            throw new IllegalArgumentException(
                    "maxParts must be at least " + Combination.MIN_PARTS + ", got " + maxParts);
        }
    }

    public static CombinationRules defaults() {
        return withoutPartLimit(DEFAULT_TARGET_LENGTH);
    }

    public static CombinationRules withoutPartLimit(int targetLength) {
        return new CombinationRules(targetLength, targetLength);
    }
}