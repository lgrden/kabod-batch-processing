package io.wegetit.kabod.datafeed.bankaccount;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class BankAccount {
    private String iban;
    private String currency;
}
