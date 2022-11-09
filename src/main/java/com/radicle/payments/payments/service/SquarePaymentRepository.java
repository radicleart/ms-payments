package com.radicle.payments.payments.service;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.radicle.payments.payments.service.domain.SquarePayment;

@Repository
public interface SquarePaymentRepository extends MongoRepository<SquarePayment, String> {

    List<SquarePayment> findByStatus(String status);

	@Query(value = "{ 'transactionData.txStatus' : ?#{[0]} }")
    List<SquarePayment> findByTransactionDataTxStatus(String txStatus);

	@Query(value = "{ 'transactionData.sender' : ?#{[0]} }")
    List<SquarePayment> findBySender(String sender);

	@Query(value = "{ 'transactionData.contractId' : ?#{[0]}, 'transactionData.sender' : ?#{[1]} }")
    List<SquarePayment> findByContractIdAndSender(String contractId, String sender);

    @Query(value = "{ 'transactionData.contractId' : ?#{[0]}, 'transactionData.sender' : ?#{[1]}, 'transactionData.txStatus' : ?#{[2]} }")
    List<SquarePayment> findByContractIdAndSenderAndTxStatus(String contractId, String sender, String txStatus);

}
