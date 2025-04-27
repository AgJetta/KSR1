package org.example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CosineTextMetric implements TextMetric {

    @Override
    public double calculate(List<String> list1, List<String> list2) {
        if ((list1 == null || list1.isEmpty()) && (list2 == null || list2.isEmpty())) {
            return 1.0; // Two empty documents are considered identical
        }
        if (list1 == null || list2 == null) {
            return 0.0; // One empty and one non-empty => no similarity
        }

        Map<String, Integer> freq1 = buildFrequencyMap(list1);
        Map<String, Integer> freq2 = buildFrequencyMap(list2);

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (String key : freq1.keySet()) {
            int val1 = freq1.getOrDefault(key, 0);
            int val2 = freq2.getOrDefault(key, 0);
            dotProduct += val1 * val2;
            norm1 += val1 * val1;
        }

        for (int val : freq2.values()) {
            norm2 += val * val;
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0; // Avoid division by zero
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    private Map<String, Integer> buildFrequencyMap(List<String> list) {
        Map<String, Integer> freqMap = new HashMap<>();
        for (String item : list) {
            freqMap.put(item, freqMap.getOrDefault(item, 0) + 1);
        }
        return freqMap;
    }
}
