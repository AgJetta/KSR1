package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DocumentLoader {

    /**
     * Loads all Reuters documents from a directory containing .sgm files
     * @param directoryPath path to directory with .sgm files
     * @return List of Document objects
     */
    public List<org.example.Document> loadDocuments(String directoryPath) {
        List<org.example.Document> documents = new ArrayList<>();
        File directory = new File(directoryPath);

        // Filter for .sgm files
        File[] sgmFiles = directory.listFiles((dir, name) -> name.endsWith(".sgm"));

        if (sgmFiles == null || sgmFiles.length == 0) {
            System.out.println("No .sgm files found in directory: " + directoryPath);
            return documents;
        }

        // Process each .sgm file
        for (File file : sgmFiles) {
            try {
                System.out.println("Processing file: " + file.getName());
                List<org.example.Document> docsFromFile = extractDocumentsFromFile(file);
                documents.addAll(docsFromFile);
                System.out.println("Extracted " + docsFromFile.size() + " documents from " + file.getName());
            } catch (IOException e) {
                System.err.println("Error processing file " + file.getName() + ": " + e.getMessage());
            }
        }

        return documents;
    }

    /**
     * Extracts all Reuters documents from a single .sgm file
     * @param file the .sgm file to process
     * @return List of Document objects
     */
    private List<org.example.Document> extractDocumentsFromFile(File file) throws IOException {
        List<org.example.Document> documents = new ArrayList<>();

        // Read the file content
        String content = new String(Files.readAllBytes(file.toPath()));

        content = fixContent(content);

        Document jsoupDoc = Jsoup.parse(content, "", Parser.xmlParser());

        Elements reutersElements = jsoupDoc.select("REUTERS");

        for (Element reuters : reutersElements) {
            try {
                String docId = reuters.attr("NEWID");

                FeatureVector features = extractFeatures(reuters);
                org.example.Document doc = new org.example.Document(docId, features);
                documents.add(doc);
            } catch (Exception e) {
                System.err.println("Error processing document: " + e.getMessage());
            }
        }

        return documents;
    }

    private String fixContent(String content) {
        // Add a root element for JSoup
        content = "<ROOT>" + content + "</ROOT>";

        // Replace problematic characters
        content = content.replaceAll("&", "&amp;");

        return content;
    }

    /**
     * Extract features from a Reuters element
     */
    private FeatureVector extractFeatures(Element reuters) {
        // Extract first person name (if any)
        String firstName = extractFirstValue(reuters.select("PEOPLE D"));

        // Extract organizations
        List<String> organizations = extractValues(reuters.select("ORGS D"));

        // Extract countries
        String popularCountry = extractFirstValue(reuters.select("PLACES D"));
        String firstCountry = popularCountry; // Same for simplicity

        // Extract topic
        String popularTopic = extractFirstValue(reuters.select("TOPICS D"));

        // Extract currencies from text
        List<String> currencies = extractCurrencies(getText(reuters));

        // Extract author
        String author = reuters.select("AUTHOR").text();

        // Extract dateline
        String dateline = reuters.select("DATELINE").text();

        // Get day of week (simplified)
        int dayOfWeek = extractDayOfWeek(reuters.select("DATE").text());

        // Count words in the body
        int wordCount = countWords(getText(reuters));

        return new FeatureVector(
                firstName,
                organizations,
                popularCountry,
                firstCountry,
                popularTopic,
                currencies,
                author,
                dateline,
                dayOfWeek,
                wordCount
        );
    }

    /**
     * Extract the first value from a list of elements
     */
    private String extractFirstValue(Elements elements) {
        if (elements == null || elements.isEmpty()) {
            return "";
        }
        return elements.first().text().trim();
    }

    /**
     * Extract all values from a list of elements
     */
    private List<String> extractValues(Elements elements) {
        if (elements == null || elements.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> values = new ArrayList<>();
        for (Element element : elements) {
            String value = element.text().trim();
            if (!value.isEmpty()) {
                values.add(value);
            }
        }
        return values;
    }

    /**
     * Extract text content from the body or title
     */
    private String getText(Element reuters) {
        String bodyText = reuters.select("BODY").text();
        if (bodyText.isEmpty()) {
            bodyText = reuters.select("TITLE").text();
        }
        return bodyText;
    }

    /**
     * Simple currency extraction using keywords
     */
    private List<String> extractCurrencies(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> currencies = new ArrayList<>();
        String lowerText = text.toLowerCase();

        String[] currencyKeywords = {"dollar", "pound", "yen", "mark", "franc", "lira", "peso", "euro"};

        for (String currency : currencyKeywords) {
            if (lowerText.contains(currency)) {
                currencies.add(currency);
            }
        }

        return currencies;
    }

    /**
     * Simple day of week extraction
     */
    private int extractDayOfWeek(String dateText) {
        String lowerDate = dateText.toLowerCase();

        if (lowerDate.contains("monday")) return 1;
        if (lowerDate.contains("tuesday")) return 2;
        if (lowerDate.contains("wednesday")) return 3;
        if (lowerDate.contains("thursday")) return 4;
        if (lowerDate.contains("friday")) return 5;
        if (lowerDate.contains("saturday")) return 6;
        if (lowerDate.contains("sunday")) return 7;

        // Default
        return 0;
    }

    /**
     * Count words in text
     */
    private int countWords(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return text.split("\\s+").length;
    }

}