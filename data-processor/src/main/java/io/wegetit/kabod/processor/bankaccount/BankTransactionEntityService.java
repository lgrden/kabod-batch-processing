package io.wegetit.kabod.processor.bankaccount;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class BankTransactionEntityService {

    private final BankTransactionEntityRepository repository;

    public List<BankTransactionEntity> saveAll(List<BankTransactionEntity> list) {
        return repository.saveAll(list);
    }
}
