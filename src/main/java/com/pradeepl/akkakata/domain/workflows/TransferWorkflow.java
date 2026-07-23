package com.pradeepl.akkakata.domain.workflows;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.workflow.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pradeepl.akkakata.domain.commands.CustomerAccountCommands.Credit;
import com.pradeepl.akkakata.domain.commands.CustomerAccountCommands.Debit;
import com.pradeepl.akkakata.domain.commands.TransferCommands.BeginTransfer;
import com.pradeepl.akkakata.domain.commands.TransferCommands.MarkCharged;
import com.pradeepl.akkakata.domain.commands.TransferCommands.MarkCompensated;
import com.pradeepl.akkakata.domain.commands.TransferCommands.MarkFailed;
import com.pradeepl.akkakata.domain.commands.TransferCommands.MarkReserved;
import com.pradeepl.akkakata.domain.commands.TransferCommands.MarkShipped;
import com.pradeepl.akkakata.domain.entities.CustomerAccountEntity;
import com.pradeepl.akkakata.domain.entities.TransferEntity;
import com.pradeepl.akkakata.domain.model.CustomerAccountState;

@ComponentId("transfer-workflow")
public class TransferWorkflow extends Workflow<TransferWorkflow.State> {

    private static final Logger log = LoggerFactory.getLogger(TransferWorkflow.class);

    public record State(
        String transferId,
        String fromCustomerId,
        String toCustomerId,
        long amountCents,
        boolean charged,
        String status,
        String failureReason
    ) {}

    public record StartTransfer(String fromCustomerId, String toCustomerId, long amountCents) {}

    private final ComponentClient client;

    public TransferWorkflow(ComponentClient client) {
        this.client = client;
    }

    @Override
    public WorkflowDef<State> definition() {
        return workflow()
            .addStep(reserveStep())
            .addStep(chargeStep())
            .addStep(shipStep())
            .addStep(compensateStep());
    }

    public Effect<String> start(StartTransfer cmd) {
        if (currentState() != null) {
            return effects().error("Workflow already started");
        }
        String transferId = commandContext().workflowId();
        log.info("[{}] start: {} -> {} amountCents={}", transferId, cmd.fromCustomerId(), cmd.toCustomerId(), cmd.amountCents());

        client.forEventSourcedEntity(transferId)
            .method(TransferEntity::begin)
            .invoke(new BeginTransfer(cmd.fromCustomerId(), cmd.toCustomerId(), cmd.amountCents()));

        return effects()
            .updateState(new State(
                transferId, cmd.fromCustomerId(), cmd.toCustomerId(), cmd.amountCents(),
                false, "STARTED", null))
            .transitionTo("reserve")
            .thenReply(transferId);
    }

    public ReadOnlyEffect<State> get() {
        if (currentState() == null) {
            return effects().error("Transfer not found");
        }
        return effects().reply(currentState());
    }

    private Step reserveStep() {
        return step("reserve")
            .call(() ->
                client.forKeyValueEntity(currentState().fromCustomerId())
                    .method(CustomerAccountEntity::get)
                    .invoke()
            )
            .andThen(CustomerAccountState.class, account -> {
                var s = currentState();
                if (account.balanceCents() >= s.amountCents()) {
                    log.info("[{}] reserve OK: balance={} needed={} -> charge",
                        s.transferId(), account.balanceCents(), s.amountCents());
                    client.forEventSourcedEntity(s.transferId())
                        .method(TransferEntity::markReserved)
                        .invoke(new MarkReserved());
                    return effects().transitionTo("charge");
                }
                log.info("[{}] reserve FAILED: balance={} needed={} -> compensate",
                    s.transferId(), account.balanceCents(), s.amountCents());
                return effects().transitionTo("compensate", "reserve:INSUFFICIENT_FUNDS");
            });
    }

    private Step chargeStep() {
        return step("charge")
            .call(() ->
                client.forKeyValueEntity(currentState().fromCustomerId())
                    .method(CustomerAccountEntity::debit)
                    .invoke(new Debit(currentState().amountCents()))
            )
            .andThen(String.class, result -> {
                var s = currentState();
                if ("OK".equals(result)) {
                    log.info("[{}] charge OK: debited {} from {} -> ship", s.transferId(), s.amountCents(), s.fromCustomerId());
                    client.forEventSourcedEntity(s.transferId())
                        .method(TransferEntity::markCharged)
                        .invoke(new MarkCharged());
                    return effects()
                        .updateState(new State(
                            s.transferId(), s.fromCustomerId(), s.toCustomerId(), s.amountCents(),
                            true, s.status(), s.failureReason()))
                        .transitionTo("ship");
                }
                log.info("[{}] charge FAILED: {} -> compensate", s.transferId(), result);
                return effects().transitionTo("compensate", "charge:" + result);
            });
    }

    private Step shipStep() {
        return step("ship")
            .call(() ->
                client.forKeyValueEntity(currentState().toCustomerId())
                    .method(CustomerAccountEntity::credit)
                    .invoke(new Credit(currentState().amountCents()))
            )
            .andThen(String.class, result -> {
                var s = currentState();
                if ("OK".equals(result)) {
                    log.info("[{}] ship OK: credited {} to {} -> COMPLETED", s.transferId(), s.amountCents(), s.toCustomerId());
                    client.forEventSourcedEntity(s.transferId())
                        .method(TransferEntity::markShipped)
                        .invoke(new MarkShipped());
                    return effects()
                        .updateState(new State(
                            s.transferId(), s.fromCustomerId(), s.toCustomerId(), s.amountCents(),
                            s.charged(), "COMPLETED", null))
                        .end();
                }
                log.info("[{}] ship FAILED: {} -> compensate", s.transferId(), result);
                return effects().transitionTo("compensate", "ship:" + result);
            });
    }

    private Step compensateStep() {
        return step("compensate")
            .call(String.class, reason -> {
                var s = currentState();
                if (s.charged()) {
                    log.info("[{}] compensate: reversing debit, crediting {} back to {}",
                        s.transferId(), s.amountCents(), s.fromCustomerId());
                    client.forKeyValueEntity(s.fromCustomerId())
                        .method(CustomerAccountEntity::credit)
                        .invoke(new Credit(s.amountCents()));
                    client.forEventSourcedEntity(s.transferId())
                        .method(TransferEntity::markCompensated)
                        .invoke(new MarkCompensated(s.amountCents()));
                } else {
                    log.info("[{}] compensate: nothing to reverse (never charged)", s.transferId());
                }
                client.forEventSourcedEntity(s.transferId())
                    .method(TransferEntity::markFailed)
                    .invoke(new MarkFailed(reason));
                return reason;
            })
            .andThen(String.class, reason -> {
                var s = currentState();
                log.info("[{}] FAILED: {}", s.transferId(), reason);
                return effects()
                    .updateState(new State(
                        s.transferId(), s.fromCustomerId(), s.toCustomerId(), s.amountCents(),
                        s.charged(), "FAILED", reason))
                    .end();
            });
    }
}
