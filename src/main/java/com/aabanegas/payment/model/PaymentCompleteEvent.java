package com.aabanegas.payment.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@EqualsAndHashCode
@ToString
public class PaymentCompleteEvent implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@NotNull
    private String clientRef;
    @NotNull
    private CreditCard creditCard;
    @NotNull
    private Double amount;

    @JsonCreator
    public PaymentCompleteEvent(@JsonProperty("clientRef") String clientRef, 
    				@JsonProperty("creditAccount") CreditCard creditCard, 
    				@JsonProperty("amount") Double amount) {
        this.clientRef = clientRef;
        this.creditCard = creditCard;
        this.amount = amount;
    }
}
