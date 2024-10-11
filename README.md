# transaction-storage
A POC implementation of a system responsible to store transactions from marketplace purchases

## How-to-run

Transaction-storage is a Java based application that uses gradle to manage its dependencies and lifecycle.

To run it locally you'll have to

1. Ensure to have JAVA 21 version installed in your environment (JAVA_HOME or IDE).
2. Ensure to have docker-compose and run docker-compose file of this project, located inside docker/ folder
3. Ensure to have gradle
4. Then we are finally prepared to run our service through commands below

We have a docker-compose file set at docker/ directory that already have everything set. 
As our application is not present in a public docker registry, someone that clones it and tries to run must generate 
respective docker image.

For that we are integrating [jib plugin](https://github.com/GoogleContainerTools/jib/tree/master/jib-gradle-plugin) 
to our application lifecycle, which allow us to generate it through

```
gradle jibDockerBuild
```

This will build and store image locally. 

Now we are able to run

```
docker-compose up -d
```
to run containers and application dependencies.

transaction-storage container should be running on port 8081 and ready to consume messages and retrieve 
its currency exchanges.

## Test

To test application we can simply run `gradle test`.

But maybe you want to play a little with our application too. For that we can use some sidecar tools.

The repeatable migration we've set already is storing our db with some transactions from 2024-10-10. 
But we can also populate it by sending transaction messages in kafka-ui. Once we start docker containers, 
you should be able to access following link on mapped 8080 port.

`http://localhost:8080/ui/clusters/local/all-topics/TRANSACTION.TOPIC`

There we can **Produce Message** and see our app consuming and processing it. Below a sample of transaction payload 
we can use

```
{
  "description": "Test",
  "amount": 23.456,
  "transactionDate": <some epoch value (optional)>
}
```

Then we can play with our API with the sample requests

```
curl --location 'http://localhost:8081/v1/transactions?exchange_currency=<country-currency>&start_date=<YYYY-MM-DD hh:mm:ss>'

curl --location 'http://localhost:8081/v1/transactions?exchange_currency=Brazil-Real&start_date=2024-10-09%2020%3A30%3A00'
```

A deeper dive into our app.

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
**TRANSACTION.TOPIC**.

Our system will not handle the transaction process per se, meaning it will just have the obligation to properly store it.

We think the message broker communication will allow upstream services to quickly communicate the effective transactions, 
asynchronously. 
This will allow a higher throughput of transactions, less computational resource required and, with some specific measures taken 
in the application and infrastructural level, better resilience.

To spare resources, we'll also _consume transactions in batch_. This way we can handle various of them at once and 
improve message consumption throughput.

```
As a future step we can think on handling DB connection errors. For that, we could add a DLQ topic to sink batches 
that failed and process them on a better time. We could also prepare our consumer to stop once this kind of error occurs
in our system.
```


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

We are assuming that those transactions will come from an internal service, that effectively processed them.
Due to that, we could consider that data is supposed to come accordingly, even so we will validate format of
- Transaction Date: We are assuming dates will be recorded based on UTC timezone. 
If unexpected empty value is identified, we'll record it from current instant. All dates will be stored as long values, 
for simplicity on storage and involving queries to ponderate with it.
- Transaction amount: It should always be present (it's a processed transaction indeed) and higher than 0. 
If validation fails we're going to log it and store a 0.0 value.
- Description: If present, should have at most 50 chars. Case invalid, we'll persist it as null.



## 2 - Retrieve stored Transaction in a Certain Currency
