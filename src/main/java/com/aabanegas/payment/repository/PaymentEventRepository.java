package com.aabanegas.payment.repository;

import org.springframework.stereotype.Repository;

import com.aabanegas.payment.model.PaymentEvent;

@Repository
public interface PaymentEventRepository extends org.springframework.data.repository.Repository<PaymentEvent, Long> {

    PaymentEvent save(PaymentEvent paymentEvent);
}
