package com.pradeepl.akkakata.domain.commands;

public final class CustomerAccountCommands {

    public record Credit(
        long amountCents
    ) {}

    public record Debit(
        long amountCents
    ) {}

    public record Freeze() {}
    public record Unfreeze() {}
}
