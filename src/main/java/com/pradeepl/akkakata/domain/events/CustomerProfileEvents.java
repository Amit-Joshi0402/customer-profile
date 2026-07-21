package com.pradeepl.akkakata.domain.events;

public sealed interface CustomerProfileEvents {

    record EducationAdded(
        String customerId,
        String entryId,
        String institution,
        String degree,
        int startYear,
        int endYear
    ) implements CustomerProfileEvents {}

    record WorkExperienceAdded(
        String customerId,
        String entryId,
        String company,
        String title,
        int startYear,
        int endYear
    ) implements CustomerProfileEvents {}
}
