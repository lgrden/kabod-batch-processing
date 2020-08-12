package io.wegetit.kabod.datafeed.bankaccount;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BankTransaction {
    private String iban;
    private LocalDateTime date;
    private BigDecimal amount;
}
