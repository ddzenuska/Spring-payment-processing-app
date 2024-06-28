package io.technical_tasks.spring_web_app_for_payment_processing.payment;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.opencsv.CSVReader;
import io.technical_tasks.spring_web_app_for_payment_processing.repository.PaymentRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.beans.BeanProperty;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentRequest {

    @NonNull
    private BigDecimal amount;
    @JsonFormat(pattern = "^(LT|LV|EE)\\d{2}[A-Z0-9]{2,30}$", shape = JsonFormat.Shape.STRING)
    private String debtorIban;

    public String getDebtorIban() {
        return debtorIban;
    }

    public void setDebtorIban(String debtorIban) {
        this.debtorIban = debtorIban;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}