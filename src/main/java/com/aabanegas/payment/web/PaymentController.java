package com.aabanegas.payment.web;

import javax.validation.Valid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.aabanegas.payment.model.Payment;
import com.aabanegas.payment.model.ValidationError;
import com.aabanegas.payment.service.PaymentService;


@RestController
@RequestMapping({ "/", "/payment" })
public class PaymentController {

	private static final Log logger = LogFactory.getLog(PaymentController.class);

    private static final String INVALID_REQUEST = "Invalid request";

    private PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = "application/json")
	public Payment processPayment(@Valid @RequestBody Payment payment) {

    	logger.info("Processing payment: " + payment);
        paymentService.makePayment(payment.getClientRef(), payment.getCreditCard(), payment.getAmount());
        return payment;
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ValidationError handleBadRequest() {

        return new ValidationError(INVALID_REQUEST);
    }
}
