package io.wegetit.kabod.processor.bankaccount;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankTransactionEntityRepository extends MongoRepository<BankTransactionEntity, String> {
}
