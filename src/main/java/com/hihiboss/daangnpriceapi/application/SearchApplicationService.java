package com.hihiboss.daangnpriceapi.application;

import com.hihiboss.daangnpriceapi.domain.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SearchApplicationService {
    private CrawlService crawlService;
    private ParseService parseService;

    private SearchingHistoryRepository searchingHistoryRepository;

    @Transactional
    public List<Article> searchArticlesByPrice(String searchingKeyword, int startPrice, int endPrice) {
        if (!validatePriceScope(startPrice, endPrice)) {
            throw new IllegalArgumentException();
        }

        List<String> crawledPages = crawlService.crawlPages(searchingKeyword);
        List<Article> articles = crawledPages.parallelStream()
                .map(page -> {
                    return parseService.parseArticles(page);
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        List<Article> searchedArticles = articles.stream()
                .filter(article -> article.isPriceContained(startPrice, endPrice))
                .collect(Collectors.toList());
        List<Long> searchedArticleIds = searchedArticles.stream()
                .map(Article::getId)
                .collect(Collectors.toList());

        searchingHistoryRepository.save(
                SearchingHistory.builder()
                        .keyword(searchingKeyword)
                        .minPrice(startPrice)
                        .maxPrice(endPrice)
                        .articleIdList(searchedArticleIds)
                        .build()
        );

        return searchedArticles;
    }

    @Transactional(readOnly = true)
    public List<SearchingHistory> getAllSearchingHistories() {
        return searchingHistoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<SearchingHistory> getSearchingHistoriesByKeyword(String keyword) {
        return searchingHistoryRepository.findByKeyword(keyword);
    }

    private Boolean validatePriceScope(int startPrice, int endPrice) {
        return (startPrice >= 0) && (endPrice >= 0) && (startPrice < endPrice);
    }
}
