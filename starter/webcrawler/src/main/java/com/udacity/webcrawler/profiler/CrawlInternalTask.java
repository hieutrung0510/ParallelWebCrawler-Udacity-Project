package com.udacity.webcrawler;

import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Pattern;

public final class CrawlInternalTask extends RecursiveTask<Boolean> {
    private final String crawlerUrl;
    private final Instant instantDeadline;
    private final int maxDepthNumber;
    private final ConcurrentMap<String, Integer> countsMap;
    private final ConcurrentSkipListSet<String> urlVisited;
    private final Clock clockTime;
    private final PageParserFactory factory;
    private final List<Pattern> ignoredurls;

    public CrawlInternalTask(
            String crawlerUrl,
            Instant instantDeadline,
            int maxDepthNumber,
            ConcurrentMap<String, Integer> countsMap,
            ConcurrentSkipListSet<String> urlVisited,
            Clock clockTime,
            PageParserFactory factory,
            List<Pattern> ignoredurls) {

        this.crawlerUrl = crawlerUrl;
        this.instantDeadline = instantDeadline;
        this.maxDepthNumber = maxDepthNumber;
        this.countsMap = countsMap;
        this.urlVisited = urlVisited;
        this.clockTime = clockTime;
        this.factory = factory;
        this.ignoredurls = ignoredurls;
    }

    @Override
    protected Boolean compute() {
        if (maxDepthNumber == 0 || clockTime.instant().isAfter(instantDeadline)) {
            return false;
        }
        for (Pattern pattern : ignoredurls) {
            if (pattern.matcher(crawlerUrl).matches()) {
                return false;
            }
        }

        if (!urlVisited.add(crawlerUrl)) {
            return false;
        }

        PageParser.Result result = factory.get(crawlerUrl).parse();
        result.getWordCounts().forEach((key, value) ->
                countsMap.merge(key, value, Integer::sum)
        );

        List<CrawlInternalTask> subtasks = new ArrayList<>();
        for (String link : result.getLinks()) {
            subtasks.add(new CrawlInternalTask(
                    link,
                    instantDeadline,
                    maxDepthNumber - 1,
                    countsMap,
                    urlVisited,
                    clockTime,
                    factory,
                    ignoredurls
            ));
        }
        invokeAll(subtasks);
        return true;
    }
}