package com.aabanegas.payment;

import static com.aabanegas.payment.util.PaymentUtil.VALID_CLIENT_REFERENCE;
import static com.aabanegas.payment.util.PaymentUtil.VALID_CREDIT_CARD_EXPIRY_MONTH;
import static com.aabanegas.payment.util.PaymentUtil.VALID_CREDIT_CARD_EXPIRY_YEAR;
import static com.aabanegas.payment.util.PaymentUtil.VALID_CREDIT_CARD_NUMBER;
import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.cassandraunit.CQLDataLoader;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.aabanegas.payment.model.Payment;
import com.aabanegas.payment.util.JwtTestTokenGenerator;
import com.aabanegas.payment.util.PaymentUtil;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;

import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.jsonwebtoken.SignatureAlgorithm;

public class PaymentATSteps {

    private static final String PROPERTY_APPLICATION_IP = "APP_IP";
    private static final String PROPERTY_APPLICATION_PORT = "APP_PORT";

    private TestRestTemplate template = new TestRestTemplate();
    private URL url;
    private Payment payment;
    private ResponseEntity<Payment> responseEntity;

    private static final String PROPERTY_CASSANDRA_IP = "CI_CASSANDRA_IP";
    private static final String PROPERTY_CASSANDRA_PORT = "CI_CASSANDRA_PORT";
    private static final String PROPERTY_CASSANDRA_KEYSPACE = "CI_CASSANDRA_KEYSPACE";

    private static final String BEARER = "Bearer ";

    private Session session;

    @Before
    public void before() throws MalformedURLException {
        String applicationIPProperty = System.getenv().get(PROPERTY_APPLICATION_IP);
        String applicationIP = applicationIPProperty != null ? applicationIPProperty : "localhost";
        String applicationPortProperty = System.getenv().get(PROPERTY_APPLICATION_PORT);
        int applicationPort = applicationPortProperty != null ? Integer.parseInt(applicationPortProperty) : 8080;
        url = new URL("http://" + applicationIP + ":" + applicationPort + "/");

        initializeCassandra();
    }

    private void initializeCassandra() {
        String cassandraIPProperty = System.getenv().get(PROPERTY_CASSANDRA_IP);
        String cassandraIP = cassandraIPProperty != null ? cassandraIPProperty : "localhost";

        String cassandraPortString = System.getenv().get(PROPERTY_CASSANDRA_PORT);
        int cassandraPort = cassandraPortString != null ? Integer.parseInt(cassandraPortString) : 9042;

        String cassandraKeyspaceProperty = System.getenv().get(PROPERTY_CASSANDRA_KEYSPACE);
        String cassandraKeyspace = cassandraKeyspaceProperty != null ? cassandraKeyspaceProperty : "payment_keyspace";

        Cluster cluster = Cluster.builder()
                .addContactPoint(cassandraIP)
                .withPort(cassandraPort)
                .build();

        session = cluster.connect(cassandraKeyspace);
        CQLDataLoader cqlDataLoader = new CQLDataLoader(session);
        cqlDataLoader.load(new ClassPathCQLDataSet("create-schema.cql"));
    }

    @When("^a user sends a valid payment$")
    public void aUserSendsAValidPayment() throws Throwable {
        Map<String, String> claims = new HashMap<>();
        claims.put("user", "TEST");
        String jwt = JwtTestTokenGenerator.generateToken(SignatureAlgorithm.HS512, "test-key", claims);
        HttpHeaders headers = new HttpHeaders();
        headers.add("AUTHORIZATION", BEARER + jwt);
        payment = PaymentUtil.createPayment(VALID_CLIENT_REFERENCE, VALID_CREDIT_CARD_NUMBER, VALID_CREDIT_CARD_EXPIRY_YEAR, VALID_CREDIT_CARD_EXPIRY_MONTH, 123.45);
        HttpEntity<Payment> entity = new HttpEntity<>(payment, headers);

        responseEntity = template.exchange(url.toString(), HttpMethod.POST, entity, Payment.class);
    }

    @Then("^a valid response is returned$")
    public void aValidResponseIsReturned() throws Throwable {
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(payment, responseEntity.getBody());
    }

    @And("^records are written to Cassandra$")
    public void recordsAreWrittenToCassandra() throws Throwable {
        ResultSet resultSet = session.execute("select * from paymentevent");
        assertEquals(2, resultSet.all().size());
    }
}
