package org.example;

import java.util.*;
import java.util.stream.Collectors;

public class KNN {

    private int k;
    private double trainRatio;
    private Set<Integer> selectedFeatureIndices;
    private DistanceMetric distanceMetric;
    private TextMeasure textMeasure;

    private List<Document> allDocuments;
    private List<Document> trainingDocuments;
    private List<Document> testDocuments;

    private Map<String, Integer> truePositives;
    private Map<String, Integer> falsePositives;
    private Map<String, Integer> falseNegatives;
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
        int trainingSize = (int) (documents.size() * trainRatio);

        this.trainingDocuments = documents.subList(0, trainingSize);
        this.testDocuments = documents.subList(trainingSize, documents.size());
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

    private double calculateDistance(Document doc1, Document doc2) {
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

    public double getAccuracy() {
        return (double) correctPredictions / testDocuments.size();
    }

    // Precision for all documents: WEIGHTED
    public double getPrecision() {
        int totalSamples = allDocuments.size();

        double weightedPrecision = 0.0;
        for (String category : truePositives.keySet()) {
            int samplesPerLabel = (int) allDocuments.stream()
                    .filter(doc -> doc.getTargetLabel().equals(category))
                    .count();
            double precision = getPrecision(category);
            weightedPrecision += (precision * samplesPerLabel) / totalSamples;
        }

        return weightedPrecision;
    }

    // Precision for a specific label
    public double getPrecision(String category) {
        int tp = truePositives.getOrDefault(category, 0);
        int fp = falsePositives.getOrDefault(category, 0);

        return tp == 0 ? 0 : (double) tp / (tp + fp);
    }

    // Recall for all documents: WEIGHTED
    public double getRecall() {
        int totalSamples = allDocuments.size();

        double weightedRecall = 0.0;
        for (String category : truePositives.keySet()) {
            int samplesPerLabel = (int) allDocuments.stream()
                    .filter(doc -> doc.getTargetLabel().equals(category))
                    .count();
            double recall = getRecall(category);
            weightedRecall += (recall * samplesPerLabel) / totalSamples;
        }

        return weightedRecall;
    }

    // Calculate recall for a specific label
    public double getRecall(String category) {
        int tp = truePositives.getOrDefault(category, 0);
        int fn = falseNegatives.getOrDefault(category, 0);

        return tp == 0 ? 0 : (double) tp / (tp + fn);
    }

    // F1 score for all docs: WEIGHTED
    public double getF1() {
        double weightedF1 = 0.0;
        for (String category : truePositives.keySet()) {
            int samplesPerLabel = (int) allDocuments.stream()
                    .filter(doc -> doc.getTargetLabel().equals(category))
                    .count();
            double f1 = getF1(category);
            weightedF1 += (f1 * samplesPerLabel) / allDocuments.size();
        }

        return weightedF1;
    }

    // F1 score for a specific label
    public double getF1(String category) {
        double precision = getPrecision(category);
        double recall = getRecall(category);

        return (precision + recall == 0) ? 0 : 2 * precision * recall / (precision + recall);
    }

    public int getK() {
        return k;
    }

    public double getTrainRatio() {
        return trainRatio;
    }

    public Set<Integer> getSelectedFeatureIndices() {
        return selectedFeatureIndices;
    }

    public DistanceMetric getDistanceMetric() {
        return distanceMetric;
    }

    public List<Document> getTrainingDocuments() {
        return trainingDocuments;
    }

    public List<Document> getTestDocuments() {
        return testDocuments;
    }

    private record DocumentDistance(Document document, double distance) {}

    public void normalizeNumericalFeatures() {
        double dayOfWeekMean = calculateMean(8);
        double dayOfWeekStdDev = calculateStandardDeviation(8, dayOfWeekMean);
        double wordCountMean = calculateMean(9);
        double wordCountStdDev = calculateStandardDeviation(9, wordCountMean);

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

    private int normalizeFeature(double value, double mean, double stdDev) {
        if (stdDev == 0) {
            return 0;
        }
        double normalized = (value - mean) / stdDev;
        return (int) Math.round(normalized);
    }
}