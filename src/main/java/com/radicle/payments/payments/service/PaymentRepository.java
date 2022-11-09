package com.radicle.payments.payments.service;

import java.util.List;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.radicle.payments.payments.api.model.DonationData;
import com.radicle.payments.payments.service.domain.Payment;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {

    // @Aggregation(pipeline = {"{ $group: { _id: $paymentType, total: { $sum: '$amountMoney.$amount' } } }"})
    @Aggregation(pipeline = "{ $match: { paymentType: 'square' } }, { $group: { _id: $paymentType, total: { $sum: '$amountMoney.amount' } } }")
    List<DonationData> findTotals(String paymentType);

    @Query(value = "{ 'paymentType' : ?#{[0]} }")
    List<Payment> findByPaymentType(String paymentType);

    @Query(value = "{ 'projectId' : ?#{[0]}, 'paymentType' : ?#{[1]} }")
    List<Payment> findByProjectIdAndPaymentType(String projectId, String paymentType);

    @Query(value = "{ 'projectId' : ?#{[0]} }")
    List<Payment> findByProjectId(String projectId);

}
