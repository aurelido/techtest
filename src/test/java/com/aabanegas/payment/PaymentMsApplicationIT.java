package com.aabanegas.payment;

import com.aabanegas.payment.model.Payment;
import com.aabanegas.payment.model.PaymentCompleteEvent;
import com.aabanegas.payment.model.ValidationError;
import com.aabanegas.payment.util.JwtTestTokenGenerator;
import com.aabanegas.payment.util.PaymentUtil;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.SignatureAlgorithm;
import org.cassandraunit.spring.CassandraDataSet;
import org.cassandraunit.spring.CassandraUnitDependencyInjectionIntegrationTestExecutionListener;
import org.cassandraunit.spring.EmbeddedCassandra;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.http.*;
import org.springframework.messaging.Message;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
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
    private Source source;

    @Autowired
    private MessageCollector messageCollector;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    Session session;

    @Value("${com.aabanegas.payments.security.jwt.signing-key-base64}")
    String base64EncodedSigningKey;

    @Value("${com.aabanegas.payments.security.jwt.client-ref:clientRef}")
    String userClaim;

    @SuppressWarnings("unchecked")
    @Test
    public void testValidRequest() throws IOException {
        checkCassandraRows(0);
        Double amount = 123.45;
        Payment payment = createValidPayment();

        //http
        ResponseEntity<Payment> paymentResponseEntity = sendRequest(payment, Payment.class, generateValidToken());
        assertEquals(HttpStatus.OK, paymentResponseEntity.getStatusCode());
        assertEquals(payment, paymentResponseEntity.getBody());

        //kafka
        Message<String> paymentEventMessage = (Message<String>) messageCollector.forChannel(source.output()).poll();
        assertNotNull(paymentEventMessage);
        PaymentCompleteEvent paymentCompleteEvent = objectMapper.readValue(paymentEventMessage.getPayload(), PaymentCompleteEvent.class);
        assertEquals(payment.getDebitAccount(), paymentCompleteEvent.getDebitAccount());
        assertEquals(payment.getCreditAccount(), paymentCompleteEvent.getCreditAccount());
        assertEquals(amount, paymentCompleteEvent.getAmount());
        //cassandra
        checkCassandraRows(2);
    }

    @Test
    public void testInvalidRequest() {
        Payment payment = PaymentUtil.createPayment(PaymentUtil.VALID_TEST_BIC, PaymentUtil.VALID_TEST_IBAN_1, PaymentUtil.VALID_TEST_BIC, PaymentUtil.VALID_TEST_IBAN_2, null);
        ResponseEntity<ValidationError> paymentResponseEntity = sendRequest(payment, ValidationError.class, generateValidToken());

        assertEquals(HttpStatus.BAD_REQUEST, paymentResponseEntity.getStatusCode());
        assertEquals(new ValidationError("Invalid request"), paymentResponseEntity.getBody());
    }

    @Test
    public void testInvalidIbanRequest() {
        Payment payment = PaymentUtil.createPayment(PaymentUtil.VALID_TEST_BIC, "INVALID", PaymentUtil.VALID_TEST_BIC, PaymentUtil.VALID_TEST_IBAN_2, 123.45);
        ResponseEntity<ValidationError> paymentResponseEntity = sendRequest(payment, ValidationError.class, generateValidToken());

        assertEquals(HttpStatus.BAD_REQUEST, paymentResponseEntity.getStatusCode());
        assertEquals(new ValidationError("Invalid request"), paymentResponseEntity.getBody());
    }

    @Test
    public void testInvalidBicRequest() {
        Payment payment = PaymentUtil.createPayment("INVALID", PaymentUtil.VALID_TEST_IBAN_1, PaymentUtil.VALID_TEST_BIC, PaymentUtil.VALID_TEST_IBAN_2, 123.45);
        ResponseEntity<ValidationError> paymentResponseEntity = sendRequest(payment, ValidationError.class, generateValidToken());

        assertEquals(HttpStatus.BAD_REQUEST, paymentResponseEntity.getStatusCode());
        assertEquals(new ValidationError("Invalid request"), paymentResponseEntity.getBody());
    }

    @Test
    public void testJWTAbsent() {
        Payment payment = createValidPayment();

        //http
        ResponseEntity<Payment> paymentResponseEntity = sendRequest(payment, Payment.class, null);
        assertEquals(HttpStatus.UNAUTHORIZED, paymentResponseEntity.getStatusCode());
    }

    @Test
    public void testJWTInvalid() {
        Payment payment = createValidPayment();
        Map<String, String> claims = new HashMap<>();
        claims.put(userClaim, "TEST");
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

    private Payment createValidPayment() {
        return PaymentUtil.createPayment(PaymentUtil.VALID_TEST_BIC, PaymentUtil.VALID_TEST_IBAN_1, PaymentUtil.VALID_TEST_BIC, PaymentUtil.VALID_TEST_IBAN_2, 123.45);
    }

    private String generateValidToken() {
        Map<String, String> claims = new HashMap<>();
        claims.put(userClaim, "TEST");
        return JwtTestTokenGenerator.generateToken(SignatureAlgorithm.HS512, base64EncodedSigningKey, claims);
    }
}
