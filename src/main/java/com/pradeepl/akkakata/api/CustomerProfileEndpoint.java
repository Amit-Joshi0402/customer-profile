package com.pradeepl.akkakata.api;

import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.client.ComponentClient;

import com.pradeepl.akkakata.domain.commands.CustomerProfileCommands.AddEducation;
import com.pradeepl.akkakata.domain.commands.CustomerProfileCommands.AddWorkExperience;
import com.pradeepl.akkakata.domain.entities.CustomerProfileEntity;
import com.pradeepl.akkakata.domain.model.CustomerProfileState;

@HttpEndpoint("/api")
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
public class CustomerProfileEndpoint {

    private final ComponentClient client;

    public CustomerProfileEndpoint(ComponentClient client) {
        this.client = client;
    }

    @Post("/customers/{customerId}/profile/education")
    public String addEducation(String customerId, AddEducation cmd) {
        return client.forEventSourcedEntity(customerId)
            .method(CustomerProfileEntity::addEducation)
            .invoke(cmd);
    }

    @Post("/customers/{customerId}/profile/work")
    public String addWorkExperience(String customerId, AddWorkExperience cmd) {
        return client.forEventSourcedEntity(customerId)
            .method(CustomerProfileEntity::addWorkExperience)
            .invoke(cmd);
    }

    @Get("/customers/{customerId}/profile")
    public CustomerProfileState getProfile(String customerId) {
        return client.forEventSourcedEntity(customerId)
            .method(CustomerProfileEntity::getProfile)
            .invoke();
    }
}
