package com.pradeepl.akkakata.domain.model;

public record CustomerAccountState(
    long balanceCents,
    boolean frozen
) {
    public static CustomerAccountState empty() {
        return new CustomerAccountState(0, false);
    }
}
