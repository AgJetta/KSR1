package org.example;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JaccardTextMeasure implements TextMeasure {

    @Override
    public double calculate(List<String> list1, List<String> list2) {
        if (list1 == null || list2 == null || (list1.isEmpty() && list2.isEmpty())) {
            return 1.0; // Consider two empty lists identical
        }

        Set<String> set1 = new HashSet<>(list1);
        Set<String> set2 = new HashSet<>(list2);

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        if (union.isEmpty()) {
            return 0.0;
        }

        return (double) intersection.size() / union.size();
    }
}
