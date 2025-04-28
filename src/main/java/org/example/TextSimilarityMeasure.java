package org.example;

public interface TextSimilarityMeasure {
    double calculateSimilarity(String text1, String text2);
}

// Cosine similarity implementation for text
class CosineSimilarity implements TextSimilarityMeasure {
    @Override
    public double calculateSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null) {
            return text1 == text2 ? 0.0 : 1.0;
        }

        if (text1.equals(text2)) {
            return 0.0;  // No distance between identical texts
        }

        // Tokenize texts
        String[] tokens1 = text1.toLowerCase().split("\\W+");
        String[] tokens2 = text2.toLowerCase().split("\\W+");

        // Create term frequency vectors
        java.util.Map<String, Integer> vector1 = new java.util.HashMap<>();
        java.util.Map<String, Integer> vector2 = new java.util.HashMap<>();

        for (String token : tokens1) {
            if (!token.isEmpty()) {
                vector1.put(token, vector1.getOrDefault(token, 0) + 1);
            }
        }

        for (String token : tokens2) {
            if (!token.isEmpty()) {
                vector2.put(token, vector2.getOrDefault(token, 0) + 1);
            }
        }

        // Create a set of all terms
        java.util.Set<String> allTerms = new java.util.HashSet<>(vector1.keySet());
        allTerms.addAll(vector2.keySet());

        // Calculate dot product
        double dotProduct = 0.0;
        double magnitude1 = 0.0;
        double magnitude2 = 0.0;

        for (String term : allTerms) {
            int freq1 = vector1.getOrDefault(term, 0);
            int freq2 = vector2.getOrDefault(term, 0);

            dotProduct += freq1 * freq2;
            magnitude1 += freq1 * freq1;
            magnitude2 += freq2 * freq2;
        }

        // Calculate cosine similarity
        double similarity = dotProduct / (Math.sqrt(magnitude1) * Math.sqrt(magnitude2));

        // Convert similarity to distance (1 - similarity)
        return 1 - similarity;
    }
}

// Jaccard similarity for text
class JaccardSimilarity implements TextSimilarityMeasure {
    @Override
    public double calculateSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null) {
            return text1 == text2 ? 0.0 : 1.0;
        }

        if (text1.equals(text2)) {
            return 0.0;  // No distance between identical texts
        }

        // Tokenize texts
        String[] tokens1 = text1.toLowerCase().split("\\W+");
        String[] tokens2 = text2.toLowerCase().split("\\W+");

        // Create sets of terms
        java.util.Set<String> set1 = new java.util.HashSet<>();
        java.util.Set<String> set2 = new java.util.HashSet<>();

        for (String token : tokens1) {
            if (!token.isEmpty()) {
                set1.add(token);
            }
        }

        for (String token : tokens2) {
            if (!token.isEmpty()) {
                set2.add(token);
            }
        }

        // Calculate intersection and union
        java.util.Set<String> intersection = new java.util.HashSet<>(set1);
        intersection.retainAll(set2);

        java.util.Set<String> union = new java.util.HashSet<>(set1);
        union.addAll(set2);

        // Calculate Jaccard similarity
        double similarity = (double) intersection.size() / union.size();

        // Convert similarity to distance (1 - similarity)
        return 1 - similarity;
    }
}
