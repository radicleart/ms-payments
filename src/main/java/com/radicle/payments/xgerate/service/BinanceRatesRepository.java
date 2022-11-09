package com.radicle.payments.xgerate.service;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.radicle.payments.xgerate.service.domain.BinanceRate;

@Repository
public interface BinanceRatesRepository extends MongoRepository<BinanceRate, String> {

}
