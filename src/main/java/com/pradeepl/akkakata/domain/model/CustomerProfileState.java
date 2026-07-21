package com.pradeepl.akkakata.domain.model;

import java.util.List;

public record CustomerProfileState(
    String customerId,
    List<EducationEntry> education,
    List<WorkEntry> workExperience
) {
    public static CustomerProfileState empty() {
        return new CustomerProfileState(null, List.of(), List.of());
    }
}
