# spring-two-data-sources-example

This repository contains a sample project written to demonstrate how to use two `DataSource`s in a Spring application.

It demonstrates three mechanisms for switching between two `DataSource`s:

1. `ThreadLocal` and `AbstractRoutingDataSource`,
2. (1) combined with annotations and AOP,
3. `TransactionTemplate`s.

The application does not implement a full web application. It consists only of a controller-like class and service-like classes.

The controller-like class, `Controller` provides `get` and `post` methods for each switching mechanism.

The service-like classes - `Service`, `AnnotatedService`, and `TxService` - provide access to the databases.
