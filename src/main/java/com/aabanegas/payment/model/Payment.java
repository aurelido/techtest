package com.aabanegas.payment.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;


@Getter
@EqualsAndHashCode
@ToString
public class Payment {

	@NotNull
    private String clientRef;
	
    @Valid
    @NotNull
    private CreditCard creditCard;
    
    @NotNull
    private Double amount;

    @JsonCreator
    public Payment(@JsonProperty("clientRef") String clientRef,
    					@JsonProperty("creditCard") CreditCard creditCard, 
    					@JsonProperty("amount") Double amount) {
    		this.clientRef = clientRef;
        this.creditCard = creditCard;
        this.amount = amount;
    }
}
