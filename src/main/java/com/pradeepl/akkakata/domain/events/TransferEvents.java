package com.pradeepl.akkakata.domain.events;

public sealed interface TransferEvents {

    record transferStarted(
        String transferId,
        String fromCustomerId,
        String toCustomerId,
        long amountCents,
        long timestampEpochMillis
    ) implements TransferEvents {}

    record transferReserved(
        String transferId,
        long timestampEpochMillis
    ) implements TransferEvents {}

    record transferCharged(
        String transferId,
        long timestampEpochMillis
    ) implements TransferEvents {}

    record transferShipped(
        String transferId,
        long timestampEpochMillis
    ) implements TransferEvents {}

    record transferCompensated(
        String transferId,
        long amountCents,
        long timestampEpochMillis
    ) implements TransferEvents {}

    record transferFailed(
        String transferId,
        String reason,
        long timestampEpochMillis
    ) implements TransferEvents {}
}
