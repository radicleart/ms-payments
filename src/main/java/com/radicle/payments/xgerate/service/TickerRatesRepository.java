package com.radicle.payments.xgerate.service;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.radicle.payments.xgerate.service.domain.TickerRate;

@Repository
public interface TickerRatesRepository extends MongoRepository<TickerRate, String> {

}
