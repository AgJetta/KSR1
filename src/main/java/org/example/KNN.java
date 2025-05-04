package org.example;

import java.util.*;
import java.util.stream.Collectors;

public class KNN {

    private final int k;
    private final double trainRatio;
    private final Set<Integer> selectedFeatureIndices;
    private final DistanceMetric distanceMetric;
    private final TextMeasure textMeasure;

    private List<Document> allDocuments;
    private List<Document> trainingDocuments;
    private List<Document> testDocuments;

    private final Map<String, Integer> truePositives;
    private final Map<String, Integer> falsePositives;
    private final Map<String, Integer> falseNegatives;
    private int correctPredictions;

    public KNN(int k, double trainRatio, Set<Integer> selectedFeatureIndices,
               DistanceMetric distanceMetric, TextMeasure textMeasure) {
        this.k = k;
        this.trainRatio = trainRatio;
        this.selectedFeatureIndices = selectedFeatureIndices;
        this.distanceMetric = distanceMetric;
        this.textMeasure = textMeasure;

        this.truePositives = new HashMap<>();
        this.falsePositives = new HashMap<>();
        this.falseNegatives = new HashMap<>();
    }

    public void splitDataset(List<Document> documents) {
        this.allDocuments = new ArrayList<>(documents);

        List<Document> shuffledDocs = new ArrayList<>(documents);
        long seed = 42L;
        Collections.shuffle(shuffledDocs, new Random(seed));

        int trainingSize = (int) (shuffledDocs.size() * trainRatio);

        this.trainingDocuments = shuffledDocs.subList(0, trainingSize);
        this.testDocuments = shuffledDocs.subList(trainingSize, shuffledDocs.size());
    }


    // Single document
    public String classify(Document document) {
        List<DocumentDistance> distances = new ArrayList<>();

        for (Document trainDoc : trainingDocuments) {
            double distance = calculateDistance(document, trainDoc);
            distances.add(new DocumentDistance(trainDoc, distance));
        }

        // Ascending sort
        distances.sort(Comparator.comparingDouble(DocumentDistance::distance));

        List<DocumentDistance> nearestNeighbors = distances.subList(0, Math.min(k, distances.size()));

        Map<String, Integer> classCounts = new HashMap<>();
        for (DocumentDistance neighbor : nearestNeighbors) {
            String docClass = neighbor.document().getTargetLabel();
            classCounts.put(docClass, classCounts.getOrDefault(docClass, 0) + 1);
        }

        return classCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("unknown");
    }

    private Double calculateDistance(Document doc1, Document doc2) {
        return distanceMetric.calculate(doc1, doc2, textMeasure, selectedFeatureIndices);
    }

    // Run classification on the test set and calculate metrics
    public void evaluateModel() {
        truePositives.clear();
        falsePositives.clear();
        falseNegatives.clear();
        correctPredictions = 0;

        Set<String> categories = allDocuments.stream()
                .map(Document::getTargetLabel)
                .collect(Collectors.toSet());

        for (String category : categories) {
            truePositives.put(category, 0);
            falsePositives.put(category, 0);
            falseNegatives.put(category, 0);
        }

        for (Document testDoc : testDocuments) {
            String actualClass = testDoc.getTargetLabel();
            String predictedClass = classify(testDoc);

            if (predictedClass.equals(actualClass)) {
                correctPredictions++;
                truePositives.put(actualClass, truePositives.get(actualClass) + 1);
            } else {
                falsePositives.put(predictedClass, falsePositives.get(predictedClass) + 1);
                falseNegatives.put(actualClass, falseNegatives.get(actualClass) + 1);
            }
        }
    }

    public Double getAccuracy() {
        return (double) correctPredictions / testDocuments.size();
    }

    // Precision for all documents: WEIGHTED
    public Double getPrecision() {
        int totalSamples = allDocuments.size();

        double weightedPrecision = 0.0;
        int notNullLabelsCount = 0;
        for (String category : truePositives.keySet()) {
            int samplesPerLabel = (int) allDocuments.stream()
                    .filter(doc -> doc.getTargetLabel().equals(category))
                    .count();
            Double precision = getPrecision(category);

            if (precision != null) {
                weightedPrecision += (precision * samplesPerLabel) / totalSamples;
                notNullLabelsCount++;
            }
        }

        if (notNullLabelsCount == 0) {
            return null;
        }

        return weightedPrecision;
    }

    // Precision for a specific label
    public Double getPrecision(String category) {
        int tp = truePositives.getOrDefault(category, 0);
        int fp = falsePositives.getOrDefault(category, 0);

        if (tp == 0 && fp == 0) {
            return null;
        }

        return (double) tp / (tp + fp);
    }

    // Recall for all documents: WEIGHTED
    public Double getRecall() {
        int totalSamples = allDocuments.size();

        double weightedRecall = 0.0;
        int notNullLabelsCount = 0;
        for (String category : truePositives.keySet()) {
            int samplesPerLabel = (int) allDocuments.stream()
                    .filter(doc -> doc.getTargetLabel().equals(category))
                    .count();
            Double recall = getRecall(category);

            if (recall != null) {
                weightedRecall += (recall * samplesPerLabel) / totalSamples;
                notNullLabelsCount++;
            }
        }

        if (notNullLabelsCount == 0) {
            return null;
        }

        return weightedRecall;
    }

    // Calculate recall for a specific label
    public Double getRecall(String category) {
        int tp = truePositives.getOrDefault(category, 0);
        int fn = falseNegatives.getOrDefault(category, 0);

        if (tp == 0 && fn == 0) {
            return null;
        }

        return (double) tp / (tp + fn);
    }

    // F1 score for all docs: WEIGHTED
    public Double getF1() {
        double weightedF1 = 0.0;
        int notNullLabelsCount = 0;
        for (String category : truePositives.keySet()) {
            int samplesPerLabel = (int) allDocuments.stream()
                    .filter(doc -> doc.getTargetLabel().equals(category))
                    .count();
            Double f1 = getF1(category);

            if (f1 != null) {
                weightedF1 += (f1 * samplesPerLabel) / allDocuments.size();
                notNullLabelsCount++;
            }
        }

        if (notNullLabelsCount == 0) {
            return null;
        }

        return weightedF1;
    }

    // F1 score for a specific label
    public Double getF1(String category) {
        Double precision = getPrecision(category);
        Double recall = getRecall(category);

        if (precision == null || recall == null) {
            return null;
        }

        return 2 * precision * recall / (precision + recall);
    }

    public List<Document> getTestDocuments() {
        return testDocuments;
    }

    private record DocumentDistance(Document document, double distance) {}

    // Calculates mean, std, applies normalization
    public void normalizeNumericalFeatures() {
        double dayOfWeekMean = calculateMean(8);
        double dayOfWeekStdDev = calculateStandardDeviation(8, dayOfWeekMean);
        double wordCountMean = calculateMean(9);
        double wordCountStdDev = calculateStandardDeviation(9, wordCountMean);

        normalizeDocuments(dayOfWeekMean, dayOfWeekStdDev, wordCountMean, wordCountStdDev, trainingDocuments);
        normalizeDocuments(dayOfWeekMean, dayOfWeekStdDev, wordCountMean, wordCountStdDev, testDocuments);
    }

    // Applies normalization
    private void normalizeDocuments(double dayOfWeekMean, double dayOfWeekStdDev, double wordCountMean, double wordCountStdDev, List<Document> trainingDocuments) {
        for (Document doc : trainingDocuments) {
            FeatureVector features = doc.getFeatures();
            double originalDayOfWeek = features.getDayOfWeek8();
            double originalWordCount = features.getWordCount9();
            double normalizedDayOfWeek = normalizeFeature(originalDayOfWeek, dayOfWeekMean, dayOfWeekStdDev);
            double normalizedWordCount = normalizeFeature(originalWordCount, wordCountMean, wordCountStdDev);
            doc.getFeatures().setDayOfWeek8(normalizedDayOfWeek);
            doc.getFeatures().setWordCount9(normalizedWordCount);
        }
    }

    private double calculateMean(int featureIndex) {
        double sum = 0.0;

        for (Document doc : trainingDocuments) {
            FeatureVector features = doc.getFeatures();
            if (featureIndex == 8) {
                sum += features.getDayOfWeek8();
            } else if (featureIndex == 9) {
                sum += features.getWordCount9();
            }
        }

        return sum / trainingDocuments.size();
    }

    private double calculateStandardDeviation(int featureIndex, double mean) {
        double sumSquaredDifferences = 0.0;

        for (Document doc : trainingDocuments) {
            FeatureVector features = doc.getFeatures();
            double value;

            if (featureIndex == 8) {
                value = features.getDayOfWeek8();
            } else if (featureIndex == 9) {
                value = features.getWordCount9();
            } else {
                throw new IllegalArgumentException("Feature index must be 8 or 9");
            }

            double difference = value - mean;
            sumSquaredDifferences += difference * difference;
        }

        double variance = sumSquaredDifferences / trainingDocuments.size();
        return Math.sqrt(variance);
    }

    private Double normalizeFeature(double value, double mean, double stdDev) {
        if (stdDev == 0) {
            return 0.0;
        }
        return (value - mean) / stdDev;
    }

    public void printClassDistribution() {
        Map<String, Long> trainingDist = trainingDocuments.stream()
                .collect(Collectors.groupingBy(Document::getTargetLabel, Collectors.counting()));

        Map<String, Long> testDist = testDocuments.stream()
                .collect(Collectors.groupingBy(Document::getTargetLabel, Collectors.counting()));

        Map<String, Long> totalDist = allDocuments.stream()
                .collect(Collectors.groupingBy(Document::getTargetLabel, Collectors.counting()));

        System.out.println("Total distribution: " + totalDist);
        System.out.println("Training distribution: " + trainingDist);
        System.out.println("Test distribution: " + testDist);
    }
}