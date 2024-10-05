# transaction-storage
A POC implementation of a system responsible to store transactions from marketplace purchases

We are working with basically two features:

## 1 - Store a Transaction

This service is intended to be part of a platform that process payments online.

One of the resources a platform like that should have is the important aspect of record transactions, the moment they occur and other significant properties that could increase system resilience and tracking of its functionalities, specially transaction related information.

As initial requirements we will consider that this service will receive data with below information

- **Description:** that must not exceed 50 characters
- **Transaction date:** date of transaction in the format YYYY-MM-DD HH:MM:SSZ
- **Transaction amount:** a positive amount rounded to the nearest cent

**Unique identifier:** will be assigned when stored to identify this transaction. 

### Service interface

As an interface we will, initially, consider a message broker to send messages to this application, that will consume, 
process and persist it.

In this case this broker will be Kafka, providing messages (transaction events) through a topic named 
_**name of the topic**_.

Our system will not handle the transaction process per se, meaning it will just have the obligation to properly store it.

We think the broker communication will allow upstream services to quickly communicate the effective transactions, 
asynchronously. 
This will allow a higher throughput of transactions, less computational resource and, with some specific measures taken 
in the application and infrastructural level, better resilience.

### Persistence

For persistence we decided to go with a relational DB, like [Postgres](https://www.postgresql.org/).

This decision was made based on below assumptions:
1. The model of the data we'll handle is well defined, so an inflexible schema for DB table is not expected to be a problem.
2. Unique informations like identifier and transaction date could be good indexes, which relational DBs work very well. 
Non-SQL DBs can have analogous features, working as indexes, but they are not that efficient or mature once we compare 
them with consolidated SQL ones.

### Error handling

We are considering 2 cases for errors to handle
1. Validation on events
2. Connection failure with external dependencies, like Kafka or Postgres.

**_To describe what we'll do in each case_**


## 2 - Retrieve stored Transaction in a Certain Currency
