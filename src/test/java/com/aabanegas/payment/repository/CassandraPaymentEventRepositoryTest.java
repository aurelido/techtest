package com.aabanegas.payment.repository;

import com.aabanegas.payment.model.PaymentEvent;
import com.aabanegas.payment.repository.CassandraPaymentEventRepository;
import com.aabanegas.payment.util.PaymentUtil;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Insert;

import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class CassandraPaymentEventRepositoryTest {
    @Test
    public void insertIfNotExists() throws Exception {
        Session session = mock(Session.class);

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.wasApplied()).thenReturn(true);
        when(session.execute(any(Insert.class))).thenReturn(resultSet);
        CassandraPaymentEventRepository paymentEventRepository = new CassandraPaymentEventRepository(session);

        paymentEventRepository.save(new PaymentEvent(PaymentUtil.VALID_TEST_IBAN_1, PaymentEvent.PaymentEventType.CREDIT, 123.45));
        verify(session).execute(any(Insert.class));
    }

}