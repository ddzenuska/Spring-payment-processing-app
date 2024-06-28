package io.technical_tasks.spring_web_app_for_payment_processing.controller;

import io.technical_tasks.spring_web_app_for_payment_processing.exceptions.NegativeAmountException;
import io.technical_tasks.spring_web_app_for_payment_processing.payment.Payment;
import io.technical_tasks.spring_web_app_for_payment_processing.payment.PaymentRequest;
import io.technical_tasks.spring_web_app_for_payment_processing.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/payments")
    public Payment createPayment(@RequestBody PaymentRequest paymentRequest, HttpServletRequest request) {
        if (paymentRequest.getAmount().signum() < 0) {
            throw new NegativeAmountException();
        } else {
            return paymentService.createPayment(paymentRequest, request);
        }
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/payment-files")
    public String csvPaymentFile(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        return paymentService.readCSVPaymentFile(file, request);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @GetMapping("/payments")
    public List<Payment> returnListOfPayments(@RequestParam(required = false) String debtorIban) {
        return paymentService.getPayments(debtorIban);
    }
}