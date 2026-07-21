package com.pradeepl.akkakata.domain.model;

public record CustomerState(
    String Id,
    String FirstName,
    String LastName,
    String email,
    boolean deleted
) {
    public static CustomerState empty () {
        return new CustomerState(null, null, null, null, false);
    }
}