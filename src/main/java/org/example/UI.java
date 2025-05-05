package org.example;

import java.util.*;
import java.util.stream.Collectors;

public class UI {
    private final Scanner scanner;
    private final List<Document> documents;
    private final String[] featureNames = {
            "firstName", "organisations", "popularCountry", "firstCity", "popularTopic",
            "currency", "author", "localisation", "dayOfWeek", "wordCount"
    };

    public UI(List<Document> documents) {
        this.scanner = new Scanner(System.in);
        this.documents = documents;
    }

    public void start() {
        System.out.println("===== KNN Classification =====");

        if (documents.isEmpty()) {
            System.err.println("No documents found. Please check your data directory path.");
            return;
        }

        int k = promptForK();
        double splitRatio = promptForSplitRatio();
        Set<Integer> selectedFeatures = promptForFeatures();
        DistanceMetric metric = promptForDistanceMetric();
        TextMeasure textMeasure = promptForTextMeasure();
        runClassifier(k, splitRatio, selectedFeatures, metric, textMeasure, documents);
    }

    private int promptForK() {
        int k = 0;
        boolean validInput = false;

        while (!validInput) {
            System.out.print("Enter k value (1-10): ");
            try {
                k = Integer.parseInt(scanner.nextLine().trim());
                if (k >= 1 && k <= 10) {
                    validInput = true;
                } else {
                    System.out.println("Please enter a value between 1 and 10.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }

        return k;
    }

    private double promptForSplitRatio() {
        double ratio = 0.0;
        boolean validInput = false;

        while (!validInput) {
            System.out.print("Enter train/test split ratio (0.1-0.99): ");
            try {
                ratio = Double.parseDouble(scanner.nextLine().trim());
                if (ratio >= 0.1 && ratio <= 0.99) {
                    validInput = true;
                } else {
                    System.out.println("Please enter a value between 0.1 and 0.99.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }

        return ratio;
    }

    private Set<Integer> promptForFeatures() {
        Set<Integer> features = new HashSet<>();
        boolean validInput = false;

        System.out.println("\nAvailable features:");
        for (int i = 0; i < featureNames.length; i++) {
            System.out.printf("%d: %s\n", i, featureNames[i]);
        }

        while (!validInput) {
            System.out.print("Enter feature indices to use (e.g., '0,2,5'): ");
            String input = scanner.nextLine().trim();

            try {
                features = Arrays.stream(input.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Integer::parseInt)
                        .filter(i -> i >= 0 && i < featureNames.length)
                        .collect(Collectors.toSet());

                if (!features.isEmpty()) {
                    validInput = true;
                } else {
                    System.out.println("Please select at least one valid feature.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter comma-separated numbers.");
            }
        }

        System.out.println("Selected features: " +
                features.stream()
                        .map(i -> featureNames[i])
                        .collect(Collectors.joining(", ")));

        return features;
    }

    private DistanceMetric promptForDistanceMetric() {
        DistanceMetric metric = null;
        boolean validInput = false;

        System.out.println("\nAvailable distance metrics:");
        System.out.println("1: Euclidean Distance");
        System.out.println("2: Manhattan Distance");
        System.out.println("3: Chebyshev Distance");

        while (!validInput) {
            System.out.print("Choose a distance metric (1-3): ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    metric = new EuclideanDistance();
                    validInput = true;
                    System.out.println("Selected: Euclidean Distance");
                    break;
                case "2":
                    metric = new ManhattanDistance();
                    validInput = true;
                    System.out.println("Selected: Manhattan Distance");
                    break;
                case "3":
                    metric = new ChebyshevDistance();
                    validInput = true;
                    System.out.println("Selected: Chebyshev Distance");
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number from 1 to 3.");
            }
        }

        return metric;
    }

    private TextMeasure promptForTextMeasure() {
        TextMeasure textMeasure = null;
        boolean validInput = false;

        System.out.println("\nAvailable text similarity measures:");
        System.out.println("1: Cosine Similarity");
        System.out.println("2: Jaccard Similarity");
        System.out.println("3: Levenshtein Similarity");

        while (!validInput) {
            System.out.print("Choose a text similarity measure (1-2): ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    textMeasure = new CosineTextMeasure();
                    validInput = true;
                    System.out.println("Selected: Cosine Similarity");
                    break;
                case "2":
                    textMeasure = new JaccardTextMeasure();
                    validInput = true;
                    System.out.println("Selected: Jaccard Similarity");
                    break;
                case "3":
                    textMeasure = new LevenshteinTextMeasure();
                    validInput = true;
                    System.out.println("Selected: Levenshtein Similarity");
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number from 1 to 2.");
            }
        }

        return textMeasure;
    }

    static KNN runClassifier(int k, double splitRatio, Set<Integer> features,
                              DistanceMetric metric, TextMeasure textMeasure, List<org.example.Document> documents) {
        System.out.println("===== Running KNN Classifier ==============================");
        System.out.println("Configuration:");
        System.out.println("- k = " + k);
        System.out.println("- Train/Test split = " + splitRatio + "/" + (1 - splitRatio));
        System.out.println("- Features: " + features.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", ")));
        System.out.println("- Distance metric: " + metric.getClass().getSimpleName());
        System.out.println("- Text measure: " + textMeasure.getClass().getSimpleName());

        System.out.println("\n");

        KNN classifier = new KNN(k, splitRatio, features, metric, textMeasure);
        classifier.splitDataset(documents);
        classifier.normalizeNumericalFeatures();
        classifier.evaluateModel();

//        printResults(classifier);
        printLatexResults(classifier);

        return classifier;
    }


    static void printResults(KNN classifier) {
        System.out.println("\nPer-class metrics:");
        Set<String> categories = classifier.getTestDocuments().stream()
                .map(Document::getTargetLabel)
                .collect(Collectors.toSet());

        for (String category : categories) {
            System.out.printf("Class '%s': Precision = %.4f, Recall = %.4f, F1 = %.4f%n",
                    category, classifier.getPrecision(category), classifier.getRecall(category), classifier.getF1(category));}

        System.out.println("Overall metrics:");
        System.out.printf("Accuracy = %.4f, Precision = %.4f, Recall = %.4f, F1 = %.4f%n",
                classifier.getAccuracy(), classifier.getPrecision(), classifier.getRecall(), classifier.getF1());
    }

    static void printLatexResults(KNN classifier) {
        Set<String> categories = classifier.getTestDocuments().stream()
                .map(Document::getTargetLabel)
                .collect(Collectors.toSet());

        System.out.println("\\begin{table}[H]\n" +
                "\\centering\n" +
                "\\caption{Test}\n" +
                "\\begin{tabular}{|c|c|c|c|c|}\n" +
                "\\hline\n" +
                "\\textbf{Kraj/Metryka} & \\textbf{Accuracy} & \\textbf{Precision} & \\textbf{Recall} & \\textbf{F1}\\\\\n" +
                "\\hline");

        for (String category : categories) {
            System.out.printf("%s & - & %.4f & %.4f & %.4f\\\\\n",
                    category, classifier.getPrecision(category), classifier.getRecall(category), classifier.getF1(category));
            System.out.println("\\hline");
        }

        System.out.printf("\\textbf{Średnia ważona} & %.4f & %.4f & %.4f & %.4f\\\\\n",
                classifier.getAccuracy(), classifier.getPrecision(), classifier.getRecall(), classifier.getF1());

        System.out.println("\\hline");
        System.out.println("\\end{tabular}\n" +
                "\\end{table}" + "\n");
    }

    public static void createAndPrintConfusionMatrix(List<String> actualLabels, List<String> predictedLabels) {
        // Create
        Map<String, Map<String, Integer>> confusionMatrix = new HashMap<>();
        for (int i = 0; i < actualLabels.size(); i++) {
            String actual = actualLabels.get(i);
            String predicted = predictedLabels.get(i);

            confusionMatrix.putIfAbsent(actual, new HashMap<>());
            confusionMatrix.get(actual).put(predicted, confusionMatrix.get(actual).getOrDefault(predicted, 0) + 1);
        }

        // Print
        System.out.println("\nConfusion Matrix:");
        Set<String> allLabels = new HashSet<>(actualLabels);
        allLabels.addAll(predictedLabels);
        List<String> sortedLabels = new ArrayList<>(allLabels);
        Collections.sort(sortedLabels);
        System.out.print("\t");
        for (String label : sortedLabels) {
            System.out.print(label + "\t");
        }
        System.out.println();
        for (String actual : sortedLabels) {
            System.out.print(actual + "\t");
            for (String predicted : sortedLabels) {
                int count = confusionMatrix.getOrDefault(actual, new HashMap<>()).getOrDefault(predicted, 0);
                System.out.print(count + "\t");
            }
            System.out.println();
        }
    }
}