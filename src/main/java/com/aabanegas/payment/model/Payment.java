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

    @Valid
    @NotNull
    private AccountReference debitAccount;
    @Valid
    @NotNull
    private AccountReference creditAccount;
    @NotNull
    private Double amount;

    //TODO: add timestamp

    @JsonCreator
    public Payment(@JsonProperty("debitAccount") AccountReference debitAccount, @JsonProperty("creditAccount") AccountReference creditAccount, @JsonProperty("amount") Double amount) {
        this.debitAccount = debitAccount;
        this.creditAccount = creditAccount;
        this.amount = amount;
    }
}
