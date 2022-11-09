package com.radicle.payments.payments.service;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.radicle.payments.payments.service.domain.OpenNodePayment;

@Repository
public interface OpenNodePaymentRepository extends MongoRepository<OpenNodePayment, String> {
    
    List<OpenNodePayment> findByStatus(String status);

	@Query(value = "{ 'transactionData.txStatus' : ?#{[0]} }")
    List<OpenNodePayment> findByTransactionDataTxStatus(String txStatus);

	@Query(value = "{ 'transactionData.sender' : ?#{[0]} }")
    List<OpenNodePayment> findBySender(String sender);

	@Query(value = "{ 'transactionData.contractId' : ?#{[0]}, 'transactionData.sender' : ?#{[1]} }")
    List<OpenNodePayment> findByContractIdAndSender(String contractId, String sender);

    @Query(value = "{ 'transactionData.contractId' : ?#{[0]}, 'transactionData.sender' : ?#{[1]}, 'transactionData.txStatus' : ?#{[2]} }")
    List<OpenNodePayment> findByContractIdAndSenderAndTxStatus(String contractId, String sender, String txStatus);

}
