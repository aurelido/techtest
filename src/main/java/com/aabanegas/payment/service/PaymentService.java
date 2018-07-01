package com.aabanegas.payment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Component;

import com.aabanegas.payment.model.CreditCard;
import com.aabanegas.payment.model.PaymentCompleteEvent;
import com.aabanegas.payment.model.PaymentEvent;
import com.aabanegas.payment.repository.PaymentEventRepository;

import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
@EnableBinding(Source.class)
@Component
public class PaymentService {

    private final Source source;

    private final PaymentEventRepository paymentEventRepository;

    private final Tracer tracer;

    @Autowired
    public PaymentService(Source source, PaymentEventRepository paymentEventRepository, Tracer tracer) {
        this.source = source;
        this.paymentEventRepository = paymentEventRepository;
        this.tracer = tracer;
    }

    public void makePayment(String clientRef, CreditCard creditCard, Double amount) {
        Span cassandraSpan = this.tracer.createSpan("Cassandra Write");
        try {
        		cassandraSpan.tag("clientRef", clientRef);
            cassandraSpan.tag("creditCard", creditCard.getCardNumber());
            writeToDatabase(clientRef, creditCard, amount);
        }finally{
            this.tracer.close(cassandraSpan);
        }

        log.info("Payment made");

        source.output().send(MessageBuilder.withPayload(new PaymentCompleteEvent(clientRef, creditCard, amount)).build());
    }

    private void writeToDatabase(String clientRef, CreditCard creditCard, Double amount) {
        paymentEventRepository.save(new PaymentEvent(clientRef, creditCard.getCardNumber(), amount));
    }
}
