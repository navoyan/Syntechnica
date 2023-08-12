# Syntechnica (server-side)

## Introduction

Syntechnica is an application that will provide a remote way to manage server files, 
run scripts in the environment of integrated third-party applications, 
and provide a convenient way to manage users and their capabilities within the system.

This is the repository of the Syntechnica server-side application. 
It will provide a RESTful (almost entirely) WEB API to interact with the application's domain.


## [API Guide](https://navoyan.github.io/Syntechnica/api-guide.html)


## Running the application

Syntechnica is written using [Spring Boot](https://projects.spring.io/spring-boot) 
which makes it easy to get it up and running.

The first step is to clone the Git repository:
```bash
$ git clone https://github.com/dyam0/Syntechnica
```

Syntechnica's development workspace includes [Docker Compose](https://docs.docker.com/compose/)
for the configuration of containerized required external applications.
Therefore, you need a working Docker platform, 
and the easiest way to set it up is to install [Docker Desktop](https://www.docker.com/products/docker-desktop/).

Once all preparations are complete, you can launch the application:
```bash
$ ./gradlew bootRun
```
On the first run, the application may start execution
when the required containers are not yet fully initialized, and raise an error.
In this case, just wait a while and try again.


## Currently used technologies

- Language
  - [Java 17](https://openjdk.org/projects/jdk/17/) - As the main programming language 
    used for the development of the application.
- Framework
  - [Spring Framework](https://spring.io/projects/spring-framework) - As the main framework 
    used for the development of the application.
  - [Spring Boot 3.1](https://spring.io/projects/spring-boot) - То bootstrap and autoconfigure the application.
- Building
  - [Gradle](https://gradle.org/) ([Kotlin DSL](https://docs.gradle.org/current/userguide/kotlin_dsl.html) scripts) -
  As a build tool for the application.
- Web
  - [Tomcat](https://tomcat.apache.org/) - As a servlet container to run the application.
  - [Spring MVC](https://docs.spring.io/spring-framework/reference/web/webmvc.html) -
    As a servlet-based web framework.
  - [Spring HATEOAS](https://spring.io/projects/spring-hateoas) - 
    To implement a RESTful API with support for hypermedia links.
- Security
  - [Spring Security](https://spring.io/projects/spring-security) - 
    To control access to different parts of the application.
  - [Spring Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server) - 
    To implement Bearer JWT token authentication and authorization.
  - [Bouncy Castle](https://www.bouncycastle.org/java.html) - For advanced cryptographic purposes, 
    mainly to create a keystore (with a self-signed certificate) that stores JWT signing key.
- Data persistence
  - [PostgreSQL 15](https://www.postgresql.org/about/) - As the main database.
  - [Spring Data JPA](https://spring.io/projects/spring-data-jpa) - 
    Тo implement the data access layer using JPA based repositories
  - [Hibernate ORM](https://hibernate.org/orm/) - As the JPA implementation for ORM.
  - [Flyway](https://flywaydb.org/) - For database versioning and migrations.
- Testing
  - [JUnit 5](https://junit.org/junit5/) - As the main testing framework.
  - [Mockito](https://site.mockito.org/) - As а mocking framework.
  - [AssertJ](https://assertj.github.io/doc/) - For more readable assertions.
- Documentation
  - [Spring REST Docs](https://spring.io/projects/spring-restdocs) - То utilize a test-driven approach to documentation.
- Caching
  - [Caffeine](https://github.com/ben-manes/caffeine) - As the main caching library
- Auxiliary libraries
  - [Lombok](https://projectlombok.org/) - To reduce boilerplate code.
  - [Hibernate Validator](https://hibernate.org/validator/) -
  To validate the passed objects for compliance with the specified constraints
- Auxiliary tools
  - [Docker Compose](https://docs.docker.com/compose/) - To containerize required external applications.
