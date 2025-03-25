package com.example.findnest.model.response.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionHistoryRes {
    private String id;
    public String transactionDate;
    private BigDecimal transferAmount;
}
