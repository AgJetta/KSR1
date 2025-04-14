package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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
                String targetLabel = extractLabel(reuters);
                System.out.print("Document ID: " + docId + ", Label: " + targetLabel);
                if (targetLabel.equals("INVALID") | targetLabel.equals("MANY OR NONE")) {
                    System.out.println(" (Invalid label)");
                    continue;
                }
                System.out.println(" (Valid label)");
                org.example.Document doc = new org.example.Document(docId, features, targetLabel);
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
        String textBody = reuters.select("BODY").text();

        String firstName = findFirstOccurrenceInBody(textBody, "people");
        List<String> organizations = findOrganizations(textBody);
        String firstCountry = findFirstOccurrenceInBody(textBody, "places");
        String popularCountry = findMostCommonCountryMentioned(textBody);
        if (popularCountry.isEmpty()) {
            popularCountry = firstCountry;
        }
        String popularTopic = findMostCommonTopicMentioned(textBody, reuters);
        List<String> currencies = extractCurrencies(getText(reuters));
        String author = reuters.select("AUTHOR").text();
        String dateline = reuters.select("DATELINE").text().split("\\s+")[0].replace(",", "");
        int dayOfWeek = extractDayOfWeek(reuters.select("DATE").text());
        int wordCount = countWords(textBody);

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

    private List<String> findOrganizations(String bodyText) {
        try {
            String[] dictionary = loadDictionaryOf("orgs");

            return Arrays.stream(bodyText.split("\\s+"))
                    .filter(word -> Arrays.asList(dictionary).contains(word.toLowerCase()))
                    .collect(Collectors.toSet()).stream().toList();
        } catch (Exception e) {
            System.err.println("Error loading dictionary or processing body: " + e.getMessage());
        }

        // Return empty list if no match is found
        return Collections.emptyList();
    }

    private String findMostCommonTopicMentioned(String bodyText, Element reuters) {
        try {
            List<Element> topicElements = reuters.select("TOPICS D").stream().toList();
            List<String> topics = new ArrayList<>();
            for (Element topic : topicElements) {
                topics.add(topic.text());
            }

            List<String> wordList = Arrays.asList(bodyText.split("\\s+"));
            wordList = wordList.stream().map(String::toLowerCase).collect(Collectors.toList());
            Map<String, Integer> topicCount = new HashMap<>();

//            int count = 0;
            for (String topic : topics) {
                int count = Collections.frequency(wordList, topic.toLowerCase());
                if (count > 0) {
                    topicCount.put(topic, count);
                }
            }
            // if max count is 1, return empty string
            // todo
            return topicCount.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("");
        } catch (Exception e) {
            System.err.println("Error loading dictionary or processing body: " + e.getMessage());
            return "";
        }
    }
    private String findMostCommonCountryMentioned(String bodyText) {
        try {
            String[] dictionary = loadDictionaryOf("places");
            List<String> wordList = Arrays.asList(bodyText.split("\\s+"));
            wordList = wordList.stream().map(String::toLowerCase).collect(Collectors.toList());
            Map<String, Integer> countryCount = new HashMap<>();

//            int count = 0;
            for (String place : dictionary) {
                int count = Collections.frequency(wordList, place.toLowerCase());
                if (count > 0) {
                    countryCount.put(place, count);
                }
            }
            // if max count is 1, return empty string
            // todo
            return countryCount.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("");
        } catch (Exception e) {
            System.err.println("Error loading dictionary or processing body: " + e.getMessage());
            return "";
        }
    }

    private String findFirstOccurrenceInBody(String bodyText, String category) {
        try {
            // Load the list of strings for comparison
            String[] dictionary = loadDictionaryOf(category);
            List<String> wordList = Arrays.asList(bodyText.split("\\s+"));
            wordList = wordList.stream().map(String::toLowerCase).collect(Collectors.toList());
            // Find the first occurrence
            for (String word : wordList) {
                for (String dictWord : dictionary) {
                    if (word.equalsIgnoreCase(dictWord)) {
                        return word;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading dictionary or processing body: " + e.getMessage());
        }

        // Return null if no match is found
        return "";
    }

    /**
     * Extract the target label from a Reuters element
     * The label is the first value in the PLACES D element
     * Return EMPTY STRING if the label is not in the list of valid labels
     */
    private String extractLabel(Element reuters) {
        List<String> validPlaces = Arrays.asList("west-germany", "usa", "france", "uk", "canada", "japan");
        Elements places = reuters.select("PLACES D");
        if (places.isEmpty() | places.size() > 1) {
            return "MANY OR NONE";
        }
        String place = places.first().text();
        if (validPlaces.contains(place.toLowerCase())) {
            return place;
        }
        return "INVALID";
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
        try {
            if (text == null || text.isEmpty()) {
                return Collections.emptyList();
            }

            List<String> currencies = new ArrayList<>();
            String lowerText = text.toLowerCase();


            String[] currencyDictionary = loadDictionaryOf("currencies");

            for (String currency : currencyDictionary) {
                if (lowerText.contains(currency)) {
                    currencies.add(currency);
                }
            }

            return currencies;
        }
        catch (Exception e) {
            System.err.println("Error loading currency dictionary: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Simple day of week extraction
     */
    private int extractDayOfWeek(String dateText){
        SimpleDateFormat sdf = new SimpleDateFormat("d-MMM-yyyy HH:mm:ss.SS", Locale.ENGLISH);
        sdf.setLenient(true); // Parsing errors otherwise

        try {
            Date date = sdf.parse(dateText);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            return calendar.get(Calendar.DAY_OF_WEEK);
        }
        catch (ParseException e) {
            System.err.println("Error parsing date: " + e.getMessage());
            return -1; // Return -1 or some error code
        }
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

    public String[] loadDictionaryOf(String category) throws Exception {
        InputStream input = getClass().getClassLoader().getResourceAsStream(category + ".txt");
        if (input == null) {
            throw new IllegalArgumentException("File not found");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            List<String> lines = reader.lines().toList();
            return lines.toArray(new String[0]);
        }
    }

    public static void main(String[] args) {
        // DEV: .SGM FILES WITH THE DATA, REPLACE FOR YOUR PATH
        String docDir = "C:\\Users\\avish\\Downloads\\reuters+21578+text+categorization+collection\\reuters21578.tar\\reuters21578";

        List<org.example.Document> documents = new ArrayList<>();
        DocumentLoader loader = new DocumentLoader();
        try {
            documents = loader.loadDocuments(docDir);
        }
        catch (Exception e) {
            System.err.println("Error loading documents: " + e.getMessage());
            System.err.println("CHECK IF THE DATA DIRECTORY IS CORRECT");
        }

        System.out.println("Loaded " + documents.size() + " documents in total.");
        if (documents.isEmpty()) {
            System.err.println("No documents found. CHECK DIRECTORY PATH OR FILES.  DocumentLoader.java -> main method");
            return;
        }

        // Display some sample data
        for (org.example.Document doc : documents) {
            FeatureVector features = doc.getFeatures();
            if (features.getPopularTopic4().isEmpty()

            ) {
                continue;
            }
            System.out.println("Document ID: " + doc.getDocumentId());
            System.out.println("Label: " + doc.getTargetLabel());
            System.out.println("First Name: " + features.getFirstName0());
            System.out.println("Organizations: " + features.getOrganisations1());
            System.out.println("Popular Country: " + features.getPopularCountry2());
            System.out.println("First Country: " + features.getFirstCountry3());
            System.out.println("Topic: " + features.getPopularTopic4());
            System.out.println("Currency: " + features.getCurrency5());
            System.out.println("Author: " + features.getAuthor6());
            System.out.println("Localisation: " + features.getLocalisation7());
            System.out.println("Day of Week: " + features.getDayOfWeek8());
            System.out.println("Word Count: " + features.getWordCount9());

            System.out.println("=========================================");
        }
        List<String> validPlaces = Arrays.asList("west-germany", "usa", "france", "uk", "canada", "japan");
        Hashtable<String, Integer> counts = new Hashtable<>();
        for (org.example.Document doc : documents) {
            String label = doc.getTargetLabel();
            if (validPlaces.contains(label)) {
                counts.put(label, counts.getOrDefault(label, 0) + 1);
            }
            else {
                counts.put(label + "(Invalid)", counts.getOrDefault("Invalid", 0) + 1);
            }
        }
        System.out.println("Counts of labels:");
        for (String label : counts.keySet()) {
            System.out.println(label + ": " + counts.get(label));
        }
        Integer totalLabels = 0;
        for (Integer count : counts.values()) {
            totalLabels += count;
        }
        System.out.println("Total labels: " + totalLabels);

        // Set of currencies that appear
        List<String> currencies = new ArrayList<>();
        for (org.example.Document doc : documents) {
            FeatureVector features = doc.getFeatures();
            List<String> currencyList = features.getCurrency5();
            for (String currency : currencyList) {
                if (!currencies.contains(currency)) {
                    currencies.add(currency);
                }
            }
        }
        System.out.println("Currencies: " + currencies);

    }
}