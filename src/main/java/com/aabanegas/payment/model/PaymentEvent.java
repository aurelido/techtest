package com.aabanegas.payment.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

@Entity
@Getter
@EqualsAndHashCode
@ToString
public class PaymentEvent implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    private String iban;
    private Date insertDate = new Date(); //TODO: create by DB?

    //TODO: payment timestamp && uniqueId?

    private PaymentEventType paymentEventType;

    private Double amount;

    public PaymentEvent(String iban, PaymentEventType paymentEventType, Double amount) {
        this.iban = iban;
        this.paymentEventType = paymentEventType;
        this.amount = amount;
    }

    public enum PaymentEventType {
        CREDIT,
        DEBIT
    }
}
