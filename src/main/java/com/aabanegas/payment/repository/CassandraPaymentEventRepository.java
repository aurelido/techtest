package com.aabanegas.payment.repository;

import com.aabanegas.payment.model.PaymentEvent;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(prefix = "cassandra", name = "enabled")
@Component
public class CassandraPaymentEventRepository implements PaymentEventRepository {

    private final Session session;

    @Autowired
    public CassandraPaymentEventRepository(Session session) {
        this.session = session;
    }

    @Override
    public PaymentEvent save(PaymentEvent paymentEvent) {
        Insert insert = QueryBuilder.insertInto("payments")
        			.value("uuid", paymentEvent.getUuid())
                .value("clientref", paymentEvent.getClientRef())
                .value("execdate", paymentEvent.getExecutionTime())
                .value("creditcard", paymentEvent.getCardNumber())
                .value("amount", paymentEvent.getAmount())
                .ifNotExists();

        ResultSet resultSet = session.execute(insert);

        if(resultSet.wasApplied()) {
            return paymentEvent;
        } else {
            throw new RuntimeException("failed to save paymentevent"); //TODO : new exception type
        }
    }
}
