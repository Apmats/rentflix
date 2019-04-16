package com.apmats.rentflix.controller;

import com.apmats.rentflix.service.RecommendationService;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class RecommendationsController {

    private final RecommendationService recommendationService;

    @Autowired
    public RecommendationsController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    // A simple route that expects a number of maximum suggestions,
    // and returns the suggested movie IDs for the user
    @PostMapping(value = "/customer/{customerId}/recommendations")
    public ResponseEntity<Map<String, List<Long>>> getRecommendations(@PathVariable Long customerId,
            @RequestBody Integer maximumSuggestions) {
        Map<Map<String, String>, Map<String, String>> respMap = new LinkedHashMap<>();
        List<Long> recommendations = recommendationService.getSuggestedMoviesWithTitles(customerId, maximumSuggestions);
        return ResponseEntity.ok(Collections.singletonMap("recommended_movie_ids", recommendations));
    }

}
