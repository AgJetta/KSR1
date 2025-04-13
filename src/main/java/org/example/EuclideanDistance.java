package org.example;

import java.util.List;

public class EuclideanDistance implements DistanceMetric {

    @Override
    public double calculate(Document doc1, Document doc2) {
        List<Integer> vec1 = doc1.getFeatures().getNumericFeatures();
        List<Integer> vec2 = doc2.getFeatures().getNumericFeatures();

        if (vec1.size() != vec2.size()) {
            throw new IllegalArgumentException("Feature vectors must be of equal length");
        }

        double sum = 0;
        for (int i = 0; i < vec1.size(); i++) {
            double diff = vec1.get(i) - vec2.get(i);
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }
}
