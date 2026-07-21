package com.pradeepl.akkakata.domain.model;

public record WorkEntry(
    String entryId,
    String company,
    String title,
    int startYear,
    int endYear
) {}
