package com.aabanegas.payment.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import com.aabanegas.payment.model.CreditCard;
import com.aabanegas.payment.model.PaymentEvent;
import com.aabanegas.payment.repository.PaymentEventRepository;
import com.aabanegas.payment.util.PaymentUtil;

public class PaymentServiceTest {

    @Mock
    private Source source;

    private PaymentService paymentService;

    @Mock
    private PaymentEventRepository paymentEventRepository;

    @Mock
    private Tracer tracer;

    @Mock
    private Span span;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(source.output()).thenReturn(mock(MessageChannel.class));
        when(tracer.createSpan(anyString())).thenReturn(span);
        paymentService = new PaymentService(source, paymentEventRepository, tracer);
    }

    @Test
    public void makePayment() throws Exception {
    	CreditCard creditCard = new CreditCard(PaymentUtil.VALID_CREDIT_CARD_NUMBER, PaymentUtil.VALID_CREDIT_CARD_EXPIRY_YEAR, PaymentUtil.VALID_CREDIT_CARD_EXPIRY_MONTH);
        paymentService.makePayment(PaymentUtil.VALID_CLIENT_REFERENCE, creditCard, 123.45);
        verify(source.output()).send(any(Message.class));
        verify(paymentEventRepository, times(2)).save(any(PaymentEvent.class));
        verify(tracer).createSpan(anyString());
        verify(span, times(2)).tag(anyString(), anyString());
        verify(tracer).close(span);
    }

}