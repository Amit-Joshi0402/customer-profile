package com.pradeepl.akkakata.api;

import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Put;
import akka.javasdk.client.ComponentClient;

import com.pradeepl.akkakata.domain.commands.CustomerAccountCommands.Credit;
import com.pradeepl.akkakata.domain.commands.CustomerAccountCommands.Debit;
import com.pradeepl.akkakata.domain.commands.CustomerAccountCommands.Freeze;
import com.pradeepl.akkakata.domain.commands.CustomerAccountCommands.Unfreeze;
import com.pradeepl.akkakata.domain.entities.CustomerAccountEntity;
import com.pradeepl.akkakata.domain.model.CustomerAccountState;

@HttpEndpoint("/api")
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
public class CustomerAccountEndpoint {

    private final ComponentClient client;

    public CustomerAccountEndpoint(ComponentClient client) {
        this.client = client;
    }

    @Put("/customers/{customerId}/account/credit")
    public String credit(String customerId, Credit cmd) {
        return client.forKeyValueEntity(customerId)
            .method(CustomerAccountEntity::credit)
            .invoke(cmd);
    }

    @Put("/customers/{customerId}/account/debit")
    public String debit(String customerId, Debit cmd) {
        return client.forKeyValueEntity(customerId)
            .method(CustomerAccountEntity::debit)
            .invoke(cmd);
    }

    @Get("/customers/{customerId}/account")
    public CustomerAccountState getAccount(String customerId) {
        return client.forKeyValueEntity(customerId)
            .method(CustomerAccountEntity::get)
            .invoke();
    }

    @Put("/customers/{customerId}/account/freeze")
    public String freeze(String customerId) {
        return client.forKeyValueEntity(customerId)
            .method(CustomerAccountEntity::freeze)
            .invoke(new Freeze());
    }

    @Put("/customers/{customerId}/account/unfreeze")
    public String unfreeze(String customerId) {
        return client.forKeyValueEntity(customerId)
            .method(CustomerAccountEntity::unfreeze)
            .invoke(new Unfreeze());
    }
}
