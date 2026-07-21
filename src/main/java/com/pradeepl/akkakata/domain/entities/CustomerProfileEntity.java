package com.pradeepl.akkakata.domain.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;

import com.pradeepl.akkakata.domain.commands.CustomerProfileCommands.AddEducation;
import com.pradeepl.akkakata.domain.commands.CustomerProfileCommands.AddWorkExperience;
import com.pradeepl.akkakata.domain.events.CustomerProfileEvents;
import com.pradeepl.akkakata.domain.events.CustomerProfileEvents.EducationAdded;
import com.pradeepl.akkakata.domain.events.CustomerProfileEvents.WorkExperienceAdded;
import com.pradeepl.akkakata.domain.model.CustomerProfileState;
import com.pradeepl.akkakata.domain.model.EducationEntry;
import com.pradeepl.akkakata.domain.model.WorkEntry;

@ComponentId("customer-profile")
public class CustomerProfileEntity extends EventSourcedEntity<CustomerProfileState, CustomerProfileEvents> {

    @Override
    public CustomerProfileState emptyState() {
        return CustomerProfileState.empty();
    }

    public Effect<String> addEducation(AddEducation cmd) {
        String customerId = commandContext().entityId();
        String entryId = UUID.randomUUID().toString();

        var event = new EducationAdded(
            customerId, entryId, cmd.institution(), cmd.degree(), cmd.startYear(), cmd.endYear());

        return effects().persist(event).thenReply(__ -> entryId);
    }

    public Effect<String> addWorkExperience(AddWorkExperience cmd) {
        String customerId = commandContext().entityId();
        String entryId = UUID.randomUUID().toString();

        var event = new WorkExperienceAdded(
            customerId, entryId, cmd.company(), cmd.title(), cmd.startYear(), cmd.endYear());

        return effects().persist(event).thenReply(__ -> entryId);
    }

    public ReadOnlyEffect<CustomerProfileState> getProfile() {
        return effects().reply(currentState());
    }

    @Override
    public CustomerProfileState applyEvent(CustomerProfileEvents event) {
        return switch (event) {
            case EducationAdded e -> {
                var education = new ArrayList<>(currentState().education());
                education.add(new EducationEntry(e.entryId(), e.institution(), e.degree(), e.startYear(), e.endYear()));
                yield new CustomerProfileState(e.customerId(), List.copyOf(education), currentState().workExperience());
            }
            case WorkExperienceAdded e -> {
                var workExperience = new ArrayList<>(currentState().workExperience());
                workExperience.add(new WorkEntry(e.entryId(), e.company(), e.title(), e.startYear(), e.endYear()));
                yield new CustomerProfileState(e.customerId(), currentState().education(), List.copyOf(workExperience));
            }
        };
    }
}
