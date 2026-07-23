package com.pradeepl.akkakata.domain.entities;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;

import com.pradeepl.akkakata.domain.commands.TransferCommands.BeginTransfer;
import com.pradeepl.akkakata.domain.commands.TransferCommands.MarkCharged;
import com.pradeepl.akkakata.domain.commands.TransferCommands.MarkCompensated;
import com.pradeepl.akkakata.domain.commands.TransferCommands.MarkFailed;
import com.pradeepl.akkakata.domain.commands.TransferCommands.MarkReserved;
import com.pradeepl.akkakata.domain.commands.TransferCommands.MarkShipped;
import com.pradeepl.akkakata.domain.events.TransferEvents;
import com.pradeepl.akkakata.domain.events.TransferEvents.transferCharged;
import com.pradeepl.akkakata.domain.events.TransferEvents.transferCompensated;
import com.pradeepl.akkakata.domain.events.TransferEvents.transferFailed;
import com.pradeepl.akkakata.domain.events.TransferEvents.transferReserved;
import com.pradeepl.akkakata.domain.events.TransferEvents.transferShipped;
import com.pradeepl.akkakata.domain.events.TransferEvents.transferStarted;
import com.pradeepl.akkakata.domain.model.TransferState;
import com.pradeepl.akkakata.domain.model.TransferStatus;

@ComponentId("transfer")
public class TransferEntity extends EventSourcedEntity<TransferState, TransferEvents> {

    @Override
    public TransferState emptyState() {
        return TransferState.empty();
    }

    public Effect<String> begin(BeginTransfer cmd) {
        if (currentState().transferId() != null) {
            return effects().error("Transfer already exists");
        }
        String transferId = commandContext().entityId();
        var event = new transferStarted(
            transferId, cmd.fromCustomerId(), cmd.toCustomerId(), cmd.amountCents(), System.currentTimeMillis());
        return effects().persist(event).thenReply(__ -> transferId);
    }

    public Effect<String> markReserved(MarkReserved cmd) {
        var event = new transferReserved(currentState().transferId(), System.currentTimeMillis());
        return effects().persist(event).thenReply(__ -> "OK");
    }

    public Effect<String> markCharged(MarkCharged cmd) {
        var event = new transferCharged(currentState().transferId(), System.currentTimeMillis());
        return effects().persist(event).thenReply(__ -> "OK");
    }

    public Effect<String> markShipped(MarkShipped cmd) {
        var event = new transferShipped(currentState().transferId(), System.currentTimeMillis());
        return effects().persist(event).thenReply(__ -> "OK");
    }

    public Effect<String> markCompensated(MarkCompensated cmd) {
        var event = new transferCompensated(currentState().transferId(), cmd.amountCents(), System.currentTimeMillis());
        return effects().persist(event).thenReply(__ -> "OK");
    }

    public Effect<String> markFailed(MarkFailed cmd) {
        var event = new transferFailed(currentState().transferId(), cmd.reason(), System.currentTimeMillis());
        return effects().persist(event).thenReply(__ -> "OK");
    }

    public ReadOnlyEffect<TransferState> get() {
        return effects().reply(currentState());
    }

    @Override
    public TransferState applyEvent(TransferEvents event) {
        return switch (event) {
            case transferStarted e -> new TransferState(
                    e.transferId(), e.fromCustomerId(), e.toCustomerId(), e.amountCents(),
                    TransferStatus.STARTED, null, java.util.List.of())
                .appendHistory("STARTED", "transfer initiated", e.timestampEpochMillis());
            case transferReserved e -> currentState()
                .withStatus(TransferStatus.RESERVED)
                .appendHistory("RESERVED", "source balance covers amount", e.timestampEpochMillis());
            case transferCharged e -> currentState()
                .withStatus(TransferStatus.CHARGED)
                .appendHistory("CHARGED", "debited source account", e.timestampEpochMillis());
            case transferShipped e -> currentState()
                .withStatus(TransferStatus.COMPLETED)
                .appendHistory("COMPLETED", "credited destination account", e.timestampEpochMillis());
            case transferCompensated e -> currentState()
                .appendHistory("COMPENSATED",
                    "reversed debit, credited " + e.amountCents() + " back to " + currentState().fromCustomerId(),
                    e.timestampEpochMillis());
            case transferFailed e -> currentState()
                .withFailure(e.reason())
                .appendHistory("FAILED", e.reason(), e.timestampEpochMillis());
        };
    }
}
