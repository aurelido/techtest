package com.aabanegas.payment.model;

import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.joda.time.YearMonth;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditCard implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@NotNull
	@Pattern(regexp = "[0-9]{16}", message= "Payment card numbers are composed of 16 digits")
	String cardNumber;
	
	@JsonFormat(shape = Shape.STRING, pattern = "YYYY-MM")
	@NotNull
	private YearMonth expiryDate;

	public CreditCard(String cardNumber,int expiryYear,int expiryMonth) {
		this.cardNumber = cardNumber;
		this.expiryDate = new YearMonth(expiryYear, expiryMonth);
	}

	/**
	 * Generate the JSON representation
	 */
	@JsonValue
	public String toString() {
		return String.join(",", "expiryMonth", String.valueOf(expiryDate.getMonthOfYear()), 
				"expiryYear", String.valueOf(expiryDate.getYear()));
	}
}
