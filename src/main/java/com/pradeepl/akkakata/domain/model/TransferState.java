package com.pradeepl.akkakata.domain.model;

import java.util.List;

public record TransferState(
    String transferId,
    String fromCustomerId,
    String toCustomerId,
    long amountCents,
    TransferStatus status,
    String failureReason,
    List<HistoryEntry> history
) {
    public record HistoryEntry(String step, String detail, long timestampEpochMillis) {}

    public static TransferState empty() {
        return new TransferState(null, null, null, 0, null, null, List.of());
    }

    public TransferState appendHistory(String step, String detail, long timestampEpochMillis) {
        var next = new java.util.ArrayList<>(history);
        next.add(new HistoryEntry(step, detail, timestampEpochMillis));
        return new TransferState(transferId, fromCustomerId, toCustomerId, amountCents, status, failureReason, List.copyOf(next));
    }

    public TransferState withStatus(TransferStatus next) {
        return new TransferState(transferId, fromCustomerId, toCustomerId, amountCents, next, failureReason, history);
    }

    public TransferState withFailure(String reason) {
        return new TransferState(transferId, fromCustomerId, toCustomerId, amountCents, TransferStatus.FAILED, reason, history);
    }
}
