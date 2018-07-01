package com.aabanegas.payment.util;

import java.util.HashMap;
import java.util.Map;

import com.aabanegas.payment.model.CreditCard;
import com.aabanegas.payment.model.Payment;

import io.jsonwebtoken.SignatureAlgorithm;

public class PaymentUtil {
	public static final String VALID_CLIENT_REFERENCE = "12345";
    public static final String VALID_CREDIT_CARD_NUMBER = "0123456789012345";
    public static final int VALID_CREDIT_CARD_EXPIRY_YEAR = 22;
    public static final int VALID_CREDIT_CARD_EXPIRY_MONTH = 12;
    
    public static final String CLAIM_CLIENT_REFERENCE = "clientRef";

    public static Payment createPayment(String clientRef, String cardNumber, int cardYear, int cardMonth, Double amount) {
    	CreditCard creditCard = new CreditCard(cardNumber, cardYear, cardMonth);
        return new Payment(clientRef, creditCard, amount);
    }
    
    public static  Payment createValidPayment(Double amount) {
        return createPayment(VALID_CLIENT_REFERENCE, VALID_CREDIT_CARD_NUMBER, VALID_CREDIT_CARD_EXPIRY_YEAR, VALID_CREDIT_CARD_EXPIRY_MONTH, amount);
    }

    public static String generateValidToken(String base64EncodedSigningKey) {
        Map<String, String> claims = new HashMap<>();
        claims.put(CLAIM_CLIENT_REFERENCE, "TEST");
        return JwtTestTokenGenerator.generateToken(SignatureAlgorithm.HS512, base64EncodedSigningKey, claims);
    }
}
