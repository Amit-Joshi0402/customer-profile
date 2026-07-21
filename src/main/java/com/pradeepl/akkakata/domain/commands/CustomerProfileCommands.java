package com.pradeepl.akkakata.domain.commands;

public final class CustomerProfileCommands {

    public record AddEducation(
        String institution,
        String degree,
        int startYear,
        int endYear
    ) {}

    public record AddWorkExperience(
        String company,
        String title,
        int startYear,
        int endYear
    ) {}
}
