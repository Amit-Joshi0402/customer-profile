package com.pradeepl.akkakata.views;

import java.util.List;

import com.pradeepl.akkakata.domain.entities.CustomerEntity;
import com.pradeepl.akkakata.domain.events.CustomerEvents.customerCreated;
import com.pradeepl.akkakata.domain.events.CustomerEvents.customerDeleted;
import com.pradeepl.akkakata.domain.events.CustomerEvents.customerEmailUpdated;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Query;
import akka.javasdk.annotations.Table;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;

@ComponentId("customerview")
public class CustomerView extends View{

    public record customerEntry(String customerId, String FirstName, String LastName, String email, boolean deleted){}
    public record customerEntries(List<customerEntry> entries) {}

    @Query("SELECT * AS entries FROM customers_table")
    public QueryEffect<customerEntries> getAll() {
         return queryResult();
    }

    @Table("customers_table")
     @Consume.FromEventSourcedEntity(CustomerEntity.class)
    public static class CustomersUpdater extends TableUpdater<customerEntry> {

        public Effect<customerEntry> onEvent(customerCreated event) {
            return effects().updateRow(new customerEntry(event.customerId(), event.FirstName(), event.LastName(), "", false));
        }

        public Effect<customerEntry> onEvent(customerDeleted event) {
            var existingEmail = rowState() != null ? rowState().email() : "";
            return effects().updateRow(new customerEntry(event.customerId(), event.FirstName(), event.LastName(), existingEmail, true));
        }

        public Effect<customerEntry> onEvent(customerEmailUpdated event) {
            var current = rowState();
            return effects().updateRow(new customerEntry(current.customerId(), current.FirstName(), current.LastName(), event.email(), current.deleted()));
        }

    }
}
