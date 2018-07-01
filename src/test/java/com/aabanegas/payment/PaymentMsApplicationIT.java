package com.aabanegas.payment;

import static com.aabanegas.payment.util.PaymentUtil.VALID_CLIENT_REFERENCE;
import static com.aabanegas.payment.util.PaymentUtil.VALID_CREDIT_CARD_EXPIRY_MONTH;
import static com.aabanegas.payment.util.PaymentUtil.VALID_CREDIT_CARD_EXPIRY_YEAR;
import static com.aabanegas.payment.util.PaymentUtil.VALID_CREDIT_CARD_NUMBER;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.cassandraunit.spring.CassandraDataSet;
import org.cassandraunit.spring.CassandraUnitDependencyInjectionIntegrationTestExecutionListener;
import org.cassandraunit.spring.EmbeddedCassandra;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.aabanegas.payment.model.Payment;
import com.aabanegas.payment.model.ValidationError;
import com.aabanegas.payment.util.JwtTestTokenGenerator;
import com.aabanegas.payment.util.PaymentUtil;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;

import io.jsonwebtoken.SignatureAlgorithm;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"cassandra.port=9142", "spring.zipkin.enabled=false"})
@ActiveProfiles("cassandra")
@EmbeddedCassandra
@CassandraDataSet(keyspace = "payment_keyspace", value = "create-schema.cql")
@TestExecutionListeners(listeners = {CassandraUnitDependencyInjectionIntegrationTestExecutionListener.class, DependencyInjectionTestExecutionListener.class})
public class PaymentMsApplicationIT {

    private static final String BEARER = "Bearer ";

    @Value("${local.server.port}")
    private String port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    Session session;

    @Value("${com.aabanegas.payments.security.jwt.signing-key-base64}")
    String base64EncodedSigningKey;

    @Test
    public void testValidRequest() throws IOException {
        checkCassandraRows(0);
        Double amount = 123.45;
        Payment payment = PaymentUtil.createValidPayment(amount);

        //http
        ResponseEntity<Payment> paymentResponseEntity = sendRequest(payment, Payment.class, PaymentUtil.generateValidToken(base64EncodedSigningKey));
        assertEquals(HttpStatus.OK, paymentResponseEntity.getStatusCode());
        assertEquals(payment, paymentResponseEntity.getBody());

        //cassandra
        checkCassandraRows(2);
    }

    @Test
    public void testInvalidRequest() {
    	Payment payment = PaymentUtil.createPayment(VALID_CLIENT_REFERENCE, VALID_CREDIT_CARD_NUMBER, VALID_CREDIT_CARD_EXPIRY_YEAR, VALID_CREDIT_CARD_EXPIRY_MONTH, null);
        ResponseEntity<ValidationError> paymentResponseEntity = sendRequest(payment, ValidationError.class, PaymentUtil.generateValidToken(base64EncodedSigningKey));

        assertEquals(HttpStatus.BAD_REQUEST, paymentResponseEntity.getStatusCode());
        assertEquals(new ValidationError("Invalid request"), paymentResponseEntity.getBody());
    }

    @Test
    public void testInvalidClientRefRequest() {
    	Payment payment = PaymentUtil.createPayment("INVALID", VALID_CREDIT_CARD_NUMBER, VALID_CREDIT_CARD_EXPIRY_YEAR, VALID_CREDIT_CARD_EXPIRY_MONTH, 123.45);
        ResponseEntity<ValidationError> paymentResponseEntity = sendRequest(payment, ValidationError.class, PaymentUtil.generateValidToken(base64EncodedSigningKey));

        assertEquals(HttpStatus.BAD_REQUEST, paymentResponseEntity.getStatusCode());
        assertEquals(new ValidationError("Invalid request"), paymentResponseEntity.getBody());
    }

    @Test
    public void testInvalidCreditCardRequest() {
        Payment payment = PaymentUtil.createPayment(VALID_CLIENT_REFERENCE, "INVALID", VALID_CREDIT_CARD_EXPIRY_YEAR, VALID_CREDIT_CARD_EXPIRY_MONTH, 123.45);
        ResponseEntity<ValidationError> paymentResponseEntity = sendRequest(payment, ValidationError.class, PaymentUtil.generateValidToken(base64EncodedSigningKey));

        assertEquals(HttpStatus.BAD_REQUEST, paymentResponseEntity.getStatusCode());
        assertEquals(new ValidationError("Invalid request"), paymentResponseEntity.getBody());
    }

    @Test
    public void testJWTAbsent() {
        Payment payment = PaymentUtil.createValidPayment(123.45);

        //http
        ResponseEntity<Payment> paymentResponseEntity = sendRequest(payment, Payment.class, null);
        assertEquals(HttpStatus.UNAUTHORIZED, paymentResponseEntity.getStatusCode());
    }

    @Test
    public void testJWTInvalid() {
        Payment payment = PaymentUtil.createValidPayment(123.45);
        Map<String, String> claims = new HashMap<>();
        claims.put(VALID_CLIENT_REFERENCE, "TEST");
        String token = JwtTestTokenGenerator.generateToken(SignatureAlgorithm.HS512, "invalid-key", claims);

        //http
        ResponseEntity<Payment> paymentResponseEntity = sendRequest(payment, Payment.class, token);
        assertEquals(HttpStatus.UNAUTHORIZED, paymentResponseEntity.getStatusCode());
    }

    private <T> ResponseEntity<T> sendRequest(Payment payment, Class<T> responseType, String jwt) {
        HttpHeaders headers = new HttpHeaders();
        if (jwt != null) {
            headers.add("AUTHORIZATION", BEARER + jwt);
        }
        HttpEntity<Payment> entity = new HttpEntity<>(payment, headers);
        return restTemplate.exchange("http://localhost:" + port + "/payment", HttpMethod.POST, entity, responseType);
    }

    private void checkCassandraRows(int rows) {
        ResultSet resultSet = session.execute("select * from paymentevent");
        assertEquals(rows, resultSet.all().size());
    }

}
