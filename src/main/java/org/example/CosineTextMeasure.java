package org.example;

import java.util.*;

public class CosineTextMeasure implements TextMeasure {

    // Method to calculate cosine similarity for two lists of words
    @Override
    public double calculate(List<String> list1, List<String> list2) {
        Map<String, Integer> freqMap1 = getWordFrequencyMap(list1);
        Map<String, Integer> freqMap2 = getWordFrequencyMap(list2);

        Set<String> allWords = new HashSet<>(freqMap1.keySet());
        allWords.addAll(freqMap2.keySet());

        // dot product
        double dotProduct = 0.0;
        for (String word : allWords) {
            int freq1 = freqMap1.getOrDefault(word, 0);
            int freq2 = freqMap2.getOrDefault(word, 0);
            dotProduct += freq1 * freq2;
        }

        // magnitudes
        double magnitude1 = Math.sqrt(freqMap1.values().stream().mapToDouble(f -> f * f).sum());
        double magnitude2 = Math.sqrt(freqMap2.values().stream().mapToDouble(f -> f * f).sum());

        if (magnitude1 == 0 || magnitude2 == 0) {
            return 0;
        }
        return dotProduct / (magnitude1 * magnitude2);
    }


    public double calculate(String list1, String list2) {
        Map<Character, Integer> freqMap1 = getCharacterFrequencyMap(list1);
        Map<Character, Integer> freqMap2 = getCharacterFrequencyMap(list2);

        Set<Character> allChars = new HashSet<>(freqMap1.keySet());
        allChars.addAll(freqMap2.keySet());

        double dotProduct = 0.0;
        for (Character c : allChars) {
            int freq1 = freqMap1.getOrDefault(c, 0);
            int freq2 = freqMap2.getOrDefault(c, 0);
            dotProduct += freq1 * freq2;
        }

        double magnitude1 = Math.sqrt(freqMap1.values().stream().mapToDouble(f -> f * f).sum());
        double magnitude2 = Math.sqrt(freqMap2.values().stream().mapToDouble(f -> f * f).sum());

        if (magnitude1 == 0 || magnitude2 == 0) {
            return 0;
        }
        return dotProduct / (magnitude1 * magnitude2);
    }

    private Map<Character, Integer> getCharacterFrequencyMap(String str) {
        Map<Character, Integer> freqMap = new HashMap<>();
        for (char c : str.toCharArray()) {
            freqMap.put(c, freqMap.getOrDefault(c, 0) + 1);
        }
        return freqMap;
    }

    private Map<String, Integer> getWordFrequencyMap(List<String> words) {
        Map<String, Integer> freqMap = new HashMap<>();
        for (String word : words) {
            word = word.toLowerCase(); // Case-insensitive comparison
            freqMap.put(word, freqMap.getOrDefault(word, 0) + 1);
        }
        return freqMap;
    }

}