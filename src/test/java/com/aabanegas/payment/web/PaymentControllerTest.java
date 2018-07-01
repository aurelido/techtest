package com.aabanegas.payment.web;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.aabanegas.payment.model.Payment;
import com.aabanegas.payment.service.PaymentService;
import com.aabanegas.payment.util.PaymentUtil;


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
        Payment payment = PaymentUtil.createValidPayment(123.45);
        Payment paymentResponse = paymentController.processPayment(payment);
        assertEquals(payment, paymentResponse);
        //verify(paymentResponse).method(any(Payment.class));
    }

}