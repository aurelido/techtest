package com.aabanegas.payment.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Component;

import com.aabanegas.payment.model.AccountReference;
import com.aabanegas.payment.model.PaymentCompleteEvent;
import com.aabanegas.payment.model.PaymentEvent;
import com.aabanegas.payment.repository.PaymentEventRepository;

@EnableBinding(Source.class)
@Component
public class PaymentService {

    private static final Log LOGGER = LogFactory.getLog(PaymentService.class);

    private final Source source;

    private final PaymentEventRepository paymentEventRepository;

    private final Tracer tracer;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public PaymentService(Source source, PaymentEventRepository paymentEventRepository, Tracer tracer) {
        this.source = source;
        this.paymentEventRepository = paymentEventRepository;
        this.tracer = tracer;
    }

    public void makePayment(AccountReference debitAccount, AccountReference creditAccount, Double amount) {
        Span cassandraSpan = this.tracer.createSpan("Cassandra Write");
        try {
            cassandraSpan.tag("debit", debitAccount.getIban());
            cassandraSpan.tag("credit", creditAccount.getIban());
            writeToDatabase(debitAccount, creditAccount, amount);
        }finally{
            this.tracer.close(cassandraSpan);
        }

        LOGGER.info("Payment made");

        source.output().send(MessageBuilder.withPayload(new PaymentCompleteEvent(debitAccount, creditAccount, amount)).build());
    }

    private void writeToDatabase(AccountReference debitAccount, AccountReference creditAccount, Double amount) {
        paymentEventRepository.save(new PaymentEvent(debitAccount.getIban(), PaymentEvent.PaymentEventType.DEBIT, amount));
        paymentEventRepository.save(new PaymentEvent(creditAccount.getIban(), PaymentEvent.PaymentEventType.CREDIT, amount));
    }
}
