package io.technical_tasks.spring_web_app_for_payment_processing.service;

import com.opencsv.CSVReader;
import io.technical_tasks.spring_web_app_for_payment_processing.payment.Payment;
import io.technical_tasks.spring_web_app_for_payment_processing.payment.PaymentRequest;
import io.technical_tasks.spring_web_app_for_payment_processing.repository.PaymentRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final GeoIPService geoIPService;
    private static final Logger logger = LoggerFactory.getLogger(GeoIPService.class);

    public PaymentService(PaymentRepository paymentRepository, GeoIPService geoIPService) {
        this.paymentRepository = paymentRepository;
        this.geoIPService = geoIPService;
    }

    @Transactional
    public Payment createPayment(PaymentRequest paymentRequest, HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }

        logger.info("Using IP address: {}", ip);

        String countryCode = geoIPService.getCountryCode(ip);

        Payment payment = new Payment();
        payment.setAmount(paymentRequest.getAmount());
        payment.setDebtorIban(paymentRequest.getDebtorIban());
        payment.setTimeStamp(LocalDateTime.now());
        payment.setCountryCode(countryCode);

        return paymentRepository.save(payment);
    }

    public List<Payment> getPayments(String debtorIban) {
        if (debtorIban == null) {
            return paymentRepository.findAll();
        } else {
            return paymentRepository.findByDebtorIban(debtorIban);
        }
    }

    @Transactional
    public String readCSVPaymentFile(MultipartFile file, HttpServletRequest request) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVReader csvReader = new CSVReader(reader)) {

            String[] nextRecord;
            List<PaymentRequest> paymentRequests = new ArrayList<>();
            csvReader.readNext();

            PaymentRequest paymentRequest = null;
            while ((nextRecord = csvReader.readNext()) != null) {
                if (nextRecord.length < 2) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid CSV format");
                }

                String amountStr = nextRecord[0].trim();
                String debtorIban = nextRecord[1].trim();

                BigDecimal amount = new BigDecimal(amountStr);

                paymentRequest = new PaymentRequest();
                paymentRequest.setAmount(amount);
                paymentRequest.setDebtorIban(debtorIban);
                paymentRequests.add(paymentRequest);
            }
            createPayment(paymentRequest, request);
            return "Payments created successfully. Payment amount: " + paymentRequest.getAmount() + " , debtor IBAN: " + paymentRequest.getDebtorIban();

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to process CSV file: " + e.getMessage());
        }
    }
}