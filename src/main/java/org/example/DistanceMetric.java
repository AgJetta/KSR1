package org.example;

import java.util.List;
import java.util.Set;

public interface DistanceMetric {
        double aggregate(List<Double> distances);

        default double calculate(Document doc1, Document doc2, TextMeasure textMeasure, Set<Integer> selectedFeatureIndices) {
                FeatureVector vec1 = doc1.getFeatures();
                FeatureVector vec2 = doc2.getFeatures();
                List<Double> distances = new java.util.ArrayList<>();

                for (Integer index : selectedFeatureIndices) {
                        switch (index) {
                                case 0:
                                        distances.add(1.0 - textMeasure.calculate(
                                                java.util.Collections.singletonList(vec1.getFirstName0()),
                                                java.util.Collections.singletonList(vec2.getFirstName0())
                                        ));
                                        break;
                                case 1:
                                        distances.add(1.0 - textMeasure.calculate(
                                                vec1.getOrganisations1(), vec2.getOrganisations1()
                                        ));
                                        break;
                                case 2:
                                        distances.add(1.0 - textMeasure.calculate(
                                                java.util.Collections.singletonList(vec1.getPopularCountry2()),
                                                java.util.Collections.singletonList(vec2.getPopularCountry2())
                                        ));
                                        break;
                                case 3:
                                        distances.add(1.0 - textMeasure.calculate(
                                                java.util.Collections.singletonList(vec1.getFirstCity3()),
                                                java.util.Collections.singletonList(vec2.getFirstCity3())
                                        ));
                                        break;
                                case 4:
                                        distances.add(1.0 - textMeasure.calculate(
                                                java.util.Collections.singletonList(vec1.getPopularTopic4()),
                                                java.util.Collections.singletonList(vec2.getPopularTopic4())
                                        ));
                                        break;
                                case 5:
                                        distances.add(1.0 - textMeasure.calculate(
                                                vec1.getCurrency5(), vec2.getCurrency5()
                                        ));
                                        break;
                                case 6:
                                        distances.add(1.0 - textMeasure.calculate(
                                                java.util.Collections.singletonList(vec1.getAuthor6()),
                                                java.util.Collections.singletonList(vec2.getAuthor6())
                                        ));
                                        break;
                                case 7:
                                        distances.add(1.0 - textMeasure.calculate(
                                                java.util.Collections.singletonList(vec1.getLocalisation7()),
                                                java.util.Collections.singletonList(vec2.getLocalisation7())
                                        ));
                                        break;
                                case 8:
                                        distances.add((double) (vec1.getDayOfWeek8() - vec2.getDayOfWeek8()));
                                        break;
                                case 9:
                                        distances.add((double) (vec1.getWordCount9() - vec2.getWordCount9()));
                                        break;
                                default:
                                        throw new IllegalArgumentException("Unknown feature index: " + index);
                        }
                }

                return aggregate(distances);
        }
}
