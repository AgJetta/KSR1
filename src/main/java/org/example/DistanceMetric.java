package org.example;

import java.util.List;
import java.util.Set;

// Interface for distance metrics
public interface DistanceMetric {
        double calculate(Document doc1, Document doc2, Set<Integer> selectedFeatures);
}

// Euclidean distance implementation
class EuclideanDistance implements DistanceMetric {
        @Override
        public double calculate(Document doc1, Document doc2, Set<Integer> selectedFeatures) {
                FeatureVector vec1 = doc1.getFeatures();
                FeatureVector vec2 = doc2.getFeatures();

                double sum = 0.0;

                // Handle numeric features (index 8 and 9)
                if (selectedFeatures.contains(8)) {
                        sum += Math.pow(vec1.getDayOfWeek8() - vec2.getDayOfWeek8(), 2);
                }

                if (selectedFeatures.contains(9)) {
                        sum += Math.pow(vec1.getWordCount9() - vec2.getWordCount9(), 2);
                }

                return Math.sqrt(sum);
        }
}

// Manhattan (Street) distance implementation
class ManhattanDistance implements DistanceMetric {
        @Override
        public double calculate(Document doc1, Document doc2, Set<Integer> selectedFeatures) {
                FeatureVector vec1 = doc1.getFeatures();
                FeatureVector vec2 = doc2.getFeatures();

                double sum = 0.0;

                // Handle numeric features (index 8 and 9)
                if (selectedFeatures.contains(8)) {
                        sum += Math.abs(vec1.getDayOfWeek8() - vec2.getDayOfWeek8());
                }

                if (selectedFeatures.contains(9)) {
                        sum += Math.abs(vec1.getWordCount9() - vec2.getWordCount9());
                }

                return sum;
        }
}

// Chebyshev distance implementation
class ChebyshevDistance implements DistanceMetric {
        @Override
        public double calculate(Document doc1, Document doc2, Set<Integer> selectedFeatures) {
                FeatureVector vec1 = doc1.getFeatures();
                FeatureVector vec2 = doc2.getFeatures();

                double maxDiff = 0.0;

                // Handle numeric features (index 8 and 9)
                if (selectedFeatures.contains(8)) {
                        maxDiff = Math.max(maxDiff, Math.abs(vec1.getDayOfWeek8() - vec2.getDayOfWeek8()));
                }

                if (selectedFeatures.contains(9)) {
                        maxDiff = Math.max(maxDiff, Math.abs(vec1.getWordCount9() - vec2.getWordCount9()));
                }

                return maxDiff;
        }
}