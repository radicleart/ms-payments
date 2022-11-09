package com.radicle.payments.xgerate.service;

import java.util.List;

import com.radicle.payments.xgerate.service.domain.BinanceRate;
import com.radicle.payments.xgerate.service.domain.TickerRate;


public interface RatesService
{
	public BinanceRate findLatestBinanceRate();

	public BinanceRate findLatestBinanceEthRate();

	public TickerRate findLatestTickerRate();

	List<BinanceRate> findBinanceRatesByCloseTime(Integer limit);

	public List<TickerRate> findTickerRatesByCloseTime(Integer limit);
}
