# Spring Boot Ticket Info Project

## How to Run 

This application is packaged as a jar which has Tomcat 8 embedded. No Tomcat or JBoss installation is necessary. You run it using the ```java -jar``` command.

* Clone this repository ```git clone https://github.com/jain7aman/ewally_test.git```
* Run ```cd ewally-test```
* Make sure you are using JDK 1.8 and Maven 3.x
* You can build the project and run the tests by running ```mvn clean package```
* Once successfully built, you can run the service by one of these two methods:
```
        java -jar -Dspring.profiles.active=test target/ticketInfo-0.0.1-SNAPSHOT.jar
or
        mvn spring-boot:run -Drun.arguments="spring.profiles.active=test"
```
* Check the stdout or boot_example.log file to make sure no exceptions are thrown

Once the application runs you should see something like this

```
2017-10-17 20:53:56.599  INFO 9684 --- [            main] s.b.c.e.t.TomcatEmbeddedServletContainer : Tomcat started on port(s): 8090 (http)
2017-10-17 20:53:56.599  INFO 9684 --- [            main]  com.ewally.test.TicketInfoApplication        : Started Application in 22.285 seconds (JVM running for 23.032)
```

## About the Service

The service is just a simple ticket REST service. It uses an in-memory database (H2) to store the data.

Here is what this little application does:

* Full integration with the latest **Spring** Framework: inversion of control, dependency injection, etc.
* Packaging as a single war with embedded container (tomcat 8): No need to install a container separately on the host just run using the ``java -jar`` command
* Demonstrates how to set up healthcheck, metrics, info, environment, etc. endpoints automatically on a configured port. Inject your own health / metrics info with a few lines of code.
* Writing a RESTful service using annotation: supports both XML and JSON request / response; simply use desired ``Accept`` header in your request
* Exception mapping from application exceptions to the right HTTP response with exception details in the body
* *Spring Data* Integration with JPA/Hibernate with just a few lines of configuration and familiar annotations. 
* Automatic CRUD functionality against the data source using Spring *Repository* pattern

Here are some endpoints you can call:

### Get information about system health, configurations, etc.

```
http://localhost:8091/env
http://localhost:8091/health
http://localhost:8091/info
http://localhost:8091/metrics
```

### Create multiple ticket resource

```
POST /ticket
Accept: application/json
Content-Type: application/json

[
    {
        "code":"23792856266000000025152001324806173140000002000",
        "expDate": "29/01/2017",
        "value": "40000"
    },
    {
        "code":"23792856266000000025152001324806173140000002001",
        "expDate": "20/03/2017",
        "value": "30000"
    }
]

RESPONSE: HTTP 201 (Created)
```
Call will only succeed if all the tickets are valid and new. If any of the ticket is invalid or already existed in our database then none of the tickets are inserted into the database.

### Create single ticket resource

```
POST /ticket/23792856266000000025152001324806173140000002001
Accept: application/json
Content-Type: application/json

{
    "expDate": "20/03/2017",
    "value": "30000"
}


RESPONSE: HTTP 201 (Created)
```
Call will only succeed if the ticket is valid and new. Ticket is created only if it has valid code and no other ticket with same code exists.


### Retrieve a ticket detail

```
http://localhost:8090/ticket/23792856266000000025152001324806173140000002000

Response: HTTP 200 OK
{
    "code": "23792856266000000025152001324806173140000002000",
    "value": "40000",
    "expirationDate": "2017-01-29",
    "barcode": "23792856266000000025152001324806173140000002",
    "ticketType": "Title_banks"
}
```

# Questions and Comments: jain7aman@gmail.com





