package org.example;

import java.util.*;

public class CosineTextMeasure implements TextMeasure {

    // Method to calculate cosine similarity for two lists of words
    @Override
    public double calculate(List<String> list1, List<String> list2) {
        // Create word frequency maps for both lists
        Map<String, Integer> freqMap1 = getWordFrequencyMap(list1);
        Map<String, Integer> freqMap2 = getWordFrequencyMap(list2);

        // Combine all unique words from both lists to form the union of vocabularies
        Set<String> allWords = new HashSet<>(freqMap1.keySet());
        allWords.addAll(freqMap2.keySet());

        // Compute the dot product of the frequency vectors
        double dotProduct = 0.0;
        for (String word : allWords) {
            int freq1 = freqMap1.getOrDefault(word, 0);
            int freq2 = freqMap2.getOrDefault(word, 0);
            dotProduct += freq1 * freq2;
        }

        // Compute the magnitudes of the frequency vectors
        double magnitude1 = Math.sqrt(freqMap1.values().stream().mapToDouble(f -> f * f).sum());
        double magnitude2 = Math.sqrt(freqMap2.values().stream().mapToDouble(f -> f * f).sum());

        // Calculate and return the cosine similarity
        if (magnitude1 == 0 || magnitude2 == 0) {
            return 0; // If either vector is zero (empty text), return similarity 0
        }
        return dotProduct / (magnitude1 * magnitude2);
    }


    public double calculate(String list1, String list2) {
        // Convert both strings to character frequency maps
        Map<Character, Integer> freqMap1 = getCharacterFrequencyMap(list1);
        Map<Character, Integer> freqMap2 = getCharacterFrequencyMap(list2);

        // Combine all unique characters from both strings to form the union of characters
        Set<Character> allChars = new HashSet<>(freqMap1.keySet());
        allChars.addAll(freqMap2.keySet());

        // Compute the dot product of the frequency vectors
        double dotProduct = 0.0;
        for (Character c : allChars) {
            int freq1 = freqMap1.getOrDefault(c, 0);
            int freq2 = freqMap2.getOrDefault(c, 0);
            dotProduct += freq1 * freq2;
        }

        // Compute the magnitudes of the frequency vectors
        double magnitude1 = Math.sqrt(freqMap1.values().stream().mapToDouble(f -> f * f).sum());
        double magnitude2 = Math.sqrt(freqMap2.values().stream().mapToDouble(f -> f * f).sum());

        // Calculate and return the cosine similarity
        if (magnitude1 == 0 || magnitude2 == 0) {
            return 0; // If either vector is zero (empty string), return similarity 0
        }
        return dotProduct / (magnitude1 * magnitude2);
    }

    // Helper method to compute a frequency map of characters from a string
    private Map<Character, Integer> getCharacterFrequencyMap(String str) {
        Map<Character, Integer> freqMap = new HashMap<>();
        for (char c : str.toCharArray()) {
            freqMap.put(c, freqMap.getOrDefault(c, 0) + 1);
        }
        return freqMap;
    }

    // Helper method to compute a frequency map of words from a list
    private Map<String, Integer> getWordFrequencyMap(List<String> words) {
        Map<String, Integer> freqMap = new HashMap<>();
        for (String word : words) {
            word = word.toLowerCase(); // Case-insensitive comparison
            freqMap.put(word, freqMap.getOrDefault(word, 0) + 1);
        }
        return freqMap;
    }

}