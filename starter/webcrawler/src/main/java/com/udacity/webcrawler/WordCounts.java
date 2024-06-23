package com.udacity.webcrawler;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Math.min;
import static java.util.stream.Collectors.toMap;

final class WordCounts {


    static Map<String, Integer> sort(Map<String, Integer> wordCounts, int popularWordCount) {
        return wordCounts.entrySet().stream()
                .sorted(new WordCountComparator())
                .limit(Math.min(popularWordCount, wordCounts.size()))
                .collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue, (k,v) -> k, LinkedHashMap::new));
    }


    private static final class WordCountComparator implements Comparator<Map.Entry<String, Integer>> {
        @Override
        public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
            if (!a.getValue().equals(b.getValue())) {
                return b.getValue() - a.getValue();
            }
            if (a.getKey().length() != b.getKey().length()) {
                return b.getKey().length() - a.getKey().length();
            }
            return a.getKey().compareTo(b.getKey());
        }
    }

    private WordCounts() {
        // This class cannot be instantiated
    }
}