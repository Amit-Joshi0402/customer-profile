package com.pradeepl.akkakata.api;

import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.client.ComponentClient;

import com.pradeepl.akkakata.domain.entities.TransferEntity;
import com.pradeepl.akkakata.domain.model.TransferState;
import com.pradeepl.akkakata.domain.workflows.TransferWorkflow;
import com.pradeepl.akkakata.domain.workflows.TransferWorkflow.StartTransfer;

import java.util.UUID;

@HttpEndpoint("/api")
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
public class TransferEndpoint {

    public record StartTransferRequest(String fromCustomerId, String toCustomerId, long amountCents) {}
    public record StartTransferResponse(String transferId, String status) {}

    private final ComponentClient client;

    public TransferEndpoint(ComponentClient client) {
        this.client = client;
    }

    @Post("/transfers")
    public StartTransferResponse start(StartTransferRequest req) {
        String transferId = "transfer-" + UUID.randomUUID();
        client.forWorkflow(transferId)
            .method(TransferWorkflow::start)
            .invoke(new StartTransfer(req.fromCustomerId(), req.toCustomerId(), req.amountCents()));

        return new StartTransferResponse(transferId, "STARTED");
    }

    @Get("/transfers/{transferId}")
    public TransferWorkflow.State get(String transferId) {
        return client.forWorkflow(transferId)
            .method(TransferWorkflow::get)
            .invoke();
    }

    @Get("/transfers/{transferId}/history")
    public TransferState history(String transferId) {
        return client.forEventSourcedEntity(transferId)
            .method(TransferEntity::get)
            .invoke();
    }
}
