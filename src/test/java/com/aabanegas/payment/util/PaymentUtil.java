package com.aabanegas.payment.util;

import com.aabanegas.payment.model.AccountReference;
import com.aabanegas.payment.model.Payment;

public class PaymentUtil {
    public static final String VALID_TEST_BIC = "ABCDEF";
    public static final String VALID_TEST_IBAN_1 = "AABBCCDD1111111111111111";
    public static final String VALID_TEST_IBAN_2 = "AABBCCDD9999999999999999";

    public static Payment createPayment(String debitBic, String debitIban, String creditBic, String creditIban, Double amount) {
        AccountReference debitAccount = new AccountReference(debitBic, debitIban);
        AccountReference creditAccount = new AccountReference(creditBic, creditIban);
        return new Payment(debitAccount, creditAccount, amount);
    }
}
