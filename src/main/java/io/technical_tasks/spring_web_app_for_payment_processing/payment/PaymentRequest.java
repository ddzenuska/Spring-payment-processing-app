package io.technical_tasks.spring_web_app_for_payment_processing.payment;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;

public class PaymentRequest {

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
