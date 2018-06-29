package com.aabanegas.payment.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@EqualsAndHashCode
@ToString
public class AccountReference {

    @NotNull
    @Pattern(regexp = "^([a-zA-Z]{2}[a-zA-Z]{4}[a-zA-Z0-9]{2}([a-zA-Z0-9]{3})?)")
    private String bic;

    @NotNull
    @Pattern(regexp = "^[a-zA-Z]{2}[0-9]{2}[a-zA-Z0-9]{4}[0-9]([a-zA-Z0-9]){6,30}")
    private String iban;

    @JsonCreator
    public AccountReference(@JsonProperty("bic") String bic, @JsonProperty("iban") String iban) {
        this.bic = bic;
        this.iban = iban;
    }
}
