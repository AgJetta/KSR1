package org.example;

import java.util.List;

public class LevenshteinTextMeasure implements TextMeasure {

    @Override
    public double calculate(List<String> list1, List<String> list2) {
        if (list1 == null || list2 == null) return 0.0;
        if (list1.isEmpty() && list2.isEmpty()) return 1.0;

        int distance = levenshtein(list1, list2);
        int maxLen = Math.max(list1.size(), list2.size());
        if (maxLen == 0) return 1.0;

        return 1.0 - ((double) distance / maxLen);
    }

    @Override
    public double calculate(String str1, String str2) {
        if (str1 == null || str2 == null) return 0.0;
        if (str1.isEmpty() && str2.isEmpty()) return 1.0;

        int distance = levenshtein(str1, str2);
        int maxLen = Math.max(str1.length(), str2.length());
        if (maxLen == 0) return 1.0;

        return 1.0 - ((double) distance / maxLen);
    }

    private int levenshtein(List<String> list1, List<String> list2) {
        int[][] dp = new int[list1.size() + 1][list2.size() + 1];

        for (int i = 0; i <= list1.size(); i++) dp[i][0] = i;
        for (int j = 0; j <= list2.size(); j++) dp[0][j] = j;

        for (int i = 1; i <= list1.size(); i++) {
            for (int j = 1; j <= list2.size(); j++) {
                int cost = list1.get(i - 1).equals(list2.get(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[list1.size()][list2.size()];
    }

    private int levenshtein(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= s2.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[s1.length()][s2.length()];
    }
}
