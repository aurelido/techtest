# Payments API

API witch consumes payments request via a REST endpoint.

# Minimum requirements

This is a java-based application, as such, requires a Java runtime environment in order to run.

- Require JDK 1.8 version or newer. 

# Development

To start the application in the dev profile, simply rum:

	´´´./mvnw´´´

# Testing

To launch the application´s tests, runÑ

	´´´./mvnw clean verify test´´´

# Using Docker to deploy deploy the API

You can use Docker to easily deploy the Payments API. To achieve this, first build a docker image of your app by running:

	´´´./mvnw package -Pdev dockerfile:build´´´

