package com.pradeepl.akkakata.domain.commands;

public final class TransferCommands {

    public record BeginTransfer(String fromCustomerId, String toCustomerId, long amountCents) {}

    public record MarkReserved() {}
    public record MarkCharged() {}
    public record MarkShipped() {}
    public record MarkCompensated(long amountCents) {}
    public record MarkFailed(String reason) {}
}
