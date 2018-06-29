package com.aabanegas.payment.web;

import static com.aabanegas.payment.util.PaymentUtil.VALID_TEST_BIC;
import static com.aabanegas.payment.util.PaymentUtil.VALID_TEST_IBAN_1;
import static com.aabanegas.payment.util.PaymentUtil.VALID_TEST_IBAN_2;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.aabanegas.payment.model.Payment;
import com.aabanegas.payment.service.PaymentService;
import com.aabanegas.payment.util.PaymentUtil;
import com.aabanegas.payment.web.PaymentController;


public class PaymentControllerTest {

    private static PaymentController paymentController;


    @Mock
    PaymentService paymentService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        paymentController = new PaymentController(paymentService);
    }

    @Test
    public void processPayment() throws Exception {
        Payment payment = PaymentUtil.createPayment(VALID_TEST_BIC, VALID_TEST_IBAN_1, VALID_TEST_BIC, VALID_TEST_IBAN_2, 123.45);
        Payment paymentResponse = paymentController.processPayment(payment);
        assertEquals(payment, paymentResponse);
        //verify(paymentResponse).method(any(Payment.class));
    }

}