package com.pradeepl.akkakata.domain.model;

public record EducationEntry(
    String entryId,
    String institution,
    String degree,
    int startYear,
    int endYear
) {}
