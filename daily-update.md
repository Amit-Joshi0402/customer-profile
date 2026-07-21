# Day 1 update — Akka SDK training

Rebuilt the Day 1 Fundamentals project (`Customer` + `CustomerPreferences`) independently from
scratch, including resolving a real offline dependency mismatch. Extended it with two self-designed
exercises: an `UpdateEmail` field on the existing entity, and a brand-new `CustomerProfile`
event-sourced entity (education/work history) with accumulating list state and handler-generated
IDs. Found and fixed two real runtime bugs — a null-in-view crash, and a single-row/multi-row view
query mismatch that only surfaced with multiple customers. Deployed the service to Akka Console
(Docker build → Container Registry → live URL) and built a Postman collection to test it
end-to-end. Still chasing one open issue: a deleted customer showing `deleted: false` in the list
view — narrowing down cause tomorrow.
