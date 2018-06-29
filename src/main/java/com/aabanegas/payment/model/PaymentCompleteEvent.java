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

    @NotNull
    private AccountReference debitAccount;
    @NotNull
    private AccountReference creditAccount;
    @NotNull
    private Double amount;

    @JsonCreator
    public PaymentCompleteEvent(@JsonProperty("debitAccount") AccountReference debitAccount, @JsonProperty("creditAccount") AccountReference creditAccount, @JsonProperty("amount") Double amount) {
        this.debitAccount = debitAccount;
        this.creditAccount = creditAccount;
        this.amount = amount;
    }
}
