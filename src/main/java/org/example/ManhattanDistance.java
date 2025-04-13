package org.example;

import java.util.List;

public class ManhattanDistance implements DistanceMetric {

    @Override
    public double calculate(Document doc1, Document doc2) {
        List<Integer> vec1 = doc1.getFeatures().getNumericFeatures();
        List<Integer> vec2 = doc2.getFeatures().getNumericFeatures();

        double sum = 0;
        for (int i = 0; i < vec1.size(); i++) {
            sum += Math.abs(vec1.get(i) - vec2.get(i));
        }
        return sum;
    }
}
