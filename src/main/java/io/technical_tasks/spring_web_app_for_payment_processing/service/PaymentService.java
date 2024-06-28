package io.technical_tasks.spring_web_app_for_payment_processing.service;

import io.technical_tasks.spring_web_app_for_payment_processing.payment.Payment;
import io.technical_tasks.spring_web_app_for_payment_processing.payment.PaymentRequest;
import io.technical_tasks.spring_web_app_for_payment_processing.repository.PaymentRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
        List<String> errors = new ArrayList<>();
        PaymentRequest paymentRequest = new PaymentRequest();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] fields = line.split(",");

                if (fields.length != 2) {
                    errors.add("Invalid line format: " + line);
                    continue;
                }

                try {
                    paymentRequest.setAmount(new BigDecimal(fields[0].trim()));
                    paymentRequest.setDebtorIban(fields[1].trim());

                    createPayment(paymentRequest, request);
                } catch (Exception e) {
                    errors.add("Error processing line: " + line + " - " + e.getMessage());
                }
            }

            if (!errors.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Some payments could not be processed: " + String.join("; ", errors));
            }

            return "Payments created successfully. Payment amount: " + paymentRequest.getAmount() + " , debtor IBAN: " + paymentRequest.getDebtorIban();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to process CSV file: " + e.getMessage());
        }
    }
}