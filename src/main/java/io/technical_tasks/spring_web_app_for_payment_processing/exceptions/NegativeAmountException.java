package io.technical_tasks.spring_web_app_for_payment_processing.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

public class NegativeAmountException extends ResponseStatusException {
    public NegativeAmountException() {
        super(HttpStatus.BAD_REQUEST, "Amount cannot be below 0");
    }
}
