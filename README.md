# Payments API

API witch consumes payments request via a REST endpoint.

# Minimum requirements

This is a java-based application, as such, requires a Java runtime environment in order to run.

- Require JDK 1.8 version or newer. 

# Development

To start the application in the dev profile, simply rum:

	´´´./mvnw´´´
	
	# Using Docker to deploy deploy the API

You can use Docker to easily deploy the Payments API. To achieve this, first build a docker image of your app by running:

	´´´./mvnw package -Pdev dockerfile:build´´´

# Testing

To launch the application´s tests, run:

	´´´./mvnw clean verify test´´´

For a manual testing, you can use the swagger UI to easily creates request to consume the API endpoint. To open the swagger definition deploy your application and navigate to,

	http://localhost:18083/swagger-ui.html

