# VEGAS - Video English General Activities Server

A Spring Boot microservice that acts a booking system and teacher management system for the Video English service.

## Prerequisites

Install Docker Desktop (to allow integration tests to run) - https://www.docker.com/products/docker-desktop/


## How to test and build

VEGAS uses gradle for its dependency and build management. When running unit tests, there are a couple of Spring profiles.

`test` - for isolated unit tests, test individual classes, uses H2 for very quick database repository tests 
`integrationtest` - for testing a collection of classes together, against a real SQL Server database

## How to spin up

If using Intellij use the profile `localdev`. This will spin up using an H2 database.
If you want to test the booking logic, then you'll need to get values for the Zoom API. Ask Alan for these.