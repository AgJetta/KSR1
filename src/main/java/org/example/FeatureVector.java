package org.example;

import java.util.List;

public class FeatureVector {

    private String firstName0;
    private List<String> organisations1;
    private String popularCountry2;
    private String firstCountry3;
    private String popularTopic4;
    private List<String> currency5;
    private String author6;
    private String localisation7;
    private int dayOfWeek8;
    private int wordCount9;

    public FeatureVector(String firstName0, List<String> organisations1, String popularCountry2, String firstCountry3, String popularTopic4, List<String> currency5, String author6, String localisation7, int dayOfWeek8, int wordCount9) {
        this.firstName0 = firstName0;
        this.organisations1 = organisations1;
        this.popularCountry2 = popularCountry2;
        this.firstCountry3 = firstCountry3;
        this.popularTopic4 = popularTopic4;
        this.currency5 = currency5;
        this.author6 = author6;
        this.localisation7 = localisation7;
        this.dayOfWeek8 = dayOfWeek8;
        this.wordCount9 = wordCount9;
    }

    public List<Integer> getNumericFeatures() {
        return List.of(dayOfWeek8, wordCount9);
    }

    public List<String> getCategoricalFeatures() {
        return List.of(firstName0, popularCountry2, firstCountry3, popularTopic4, author6, localisation7);
    }

    public List<List<String>> getMultiCategoricalFeatures() {
        return List.of(organisations1, currency5);
    }

    public String getFirstName0() {
        return firstName0;
    }

    public List<String> getOrganisations1() {
        return organisations1;
    }

    public String getPopularCountry2() {
        return popularCountry2;
    }

    public String getFirstCountry3() {
        return firstCountry3;
    }

    public String getPopularTopic4() {
        return popularTopic4;
    }

    public List<String> getCurrency5() {
        return currency5;
    }

    public String getAuthor6() {
        return author6;
    }

    public String getLocalisation7() {
        return localisation7;
    }

    public int getDayOfWeek8() {
        return dayOfWeek8;
    }

    public int getWordCount9() {
        return wordCount9;
    }
}
