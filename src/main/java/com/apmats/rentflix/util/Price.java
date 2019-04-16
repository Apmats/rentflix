package com.apmats.rentflix.util;

public enum Price {
    PREMIUM("premium", 40.0), BASIC("basic", 30.0);

    private final String name;

    // I believe SEK has no subdivisions but this variable is
    // still Double to avoid rewriting a lot of our logic in case the currency
    // changes
    private final Double priceInSEK;

    Price(String name, Double priceInSEK) {
        this.name = name;
        this.priceInSEK = priceInSEK;
    }

    public Double getPrice() {
        return priceInSEK;
    }

    public String getName() {
        return name;
    }
}