package com.aabanegas.payment.model;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.Size;

import org.joda.time.LocalDate;

import com.datastax.driver.mapping.annotations.PartitionKey;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Entity
@Getter
@EqualsAndHashCode
@ToString
public class PaymentEvent implements Serializable {
	private static final long serialVersionUID = 1L;

	public PaymentEvent(String clientRef, String cardNumber, Double amount) {
		this.clientRef = clientRef;
		this.cardNumber = cardNumber;
		this.amount = amount;
	}

	@Id
	@GeneratedValue
    @PartitionKey
    private UUID uuid;
    
    @Size(max = 5, message= "Client references are composed of 5 digits")
    String clientRef;
	
	@Size(min = 16, max = 16, message= "Payment card numbers are composed of 16 digits")
	String cardNumber;
	
	Double amount;
	
	float taxAmount;
	
	String currencyAlphaCode;

	LocalDate executionTime;

}
