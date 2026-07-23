package com.pradeepl.akkakata.domain.entities;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.keyvalueentity.KeyValueEntity;

import com.pradeepl.akkakata.domain.commands.CustomerAccountCommands.Credit;
import com.pradeepl.akkakata.domain.commands.CustomerAccountCommands.Debit;
import com.pradeepl.akkakata.domain.commands.CustomerAccountCommands.Freeze;
import com.pradeepl.akkakata.domain.commands.CustomerAccountCommands.Unfreeze;
import com.pradeepl.akkakata.domain.model.CustomerAccountState;

@ComponentId("customer-account")
public class CustomerAccountEntity extends KeyValueEntity<CustomerAccountState> {

    @Override
    public CustomerAccountState emptyState() {
        return CustomerAccountState.empty();
    }

    public Effect<String> credit(Credit cmd) {
        if (currentState().frozen()) {
            return effects().reply("FROZEN");
        }
        var next = new CustomerAccountState(currentState().balanceCents() + cmd.amountCents(), currentState().frozen());
        return effects().updateState(next).thenReply("OK");
    }

    public Effect<String> debit(Debit cmd) {
        if (currentState().frozen()) {
            return effects().reply("FROZEN");
        }
        if (cmd.amountCents() > currentState().balanceCents()) {
            return effects().reply("INSUFFICIENT_FUNDS");
        }
        var next = new CustomerAccountState(currentState().balanceCents() - cmd.amountCents(), currentState().frozen());
        return effects().updateState(next).thenReply("OK");
    }

    public Effect<String> freeze(Freeze cmd) {
        var next = new CustomerAccountState(currentState().balanceCents(), true);
        return effects().updateState(next).thenReply("OK");
    }

    public Effect<String> unfreeze(Unfreeze cmd) {
        var next = new CustomerAccountState(currentState().balanceCents(), false);
        return effects().updateState(next).thenReply("OK");
    }

    public Effect<CustomerAccountState> get() {
        return effects().reply(currentState());
    }
}
