package com.radicle.payments.payments.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestOperations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.radicle.payments.xgerate.service.BinanceRatesRepository;
import com.radicle.payments.xgerate.service.FeePredictionRepository;
import com.radicle.payments.xgerate.service.FeeRatesRepository;
import com.radicle.payments.xgerate.service.RatesService;
import com.radicle.payments.xgerate.service.TickerRatesRepository;
import com.radicle.payments.xgerate.service.domain.BinanceRate;
import com.radicle.payments.xgerate.service.domain.FeeRate;
import com.radicle.payments.xgerate.service.domain.FeeRatePrediction;
import com.radicle.payments.xgerate.service.domain.TickerRate;

@RestController
@EnableAsync
@EnableScheduling
public class RatesController {

	@Autowired private RestOperations restTemplate;
	@Autowired private FeeRatesRepository feeRatesRepository;
	@Autowired private TickerRatesRepository tickerRatesRepository;
	@Autowired private BinanceRatesRepository binanceRatesRepository;
	@Autowired private FeePredictionRepository feePredictionRepository;
	@Autowired private RatesService ratesService;
	@Value("${radicle.binance.rate-path}") String binanceRatePath;
	@Value("${radicle.ticker.rate-path}") String tickerRatePath;
	@Value("${radicle.btcfee.fee-rate}") String feeRatePath;
	@Value("${radicle.btcfee.fee-list}") String feePredictionPath;
	@Autowired private ObjectMapper mapper;
	@Autowired private SimpMessagingTemplate simpMessagingTemplate;

	@Scheduled(fixedDelay=3600000)
	public void refreshTickerRates() throws JsonProcessingException {
		List<TickerRate> rates = fetchTickerRatesInternal();
		tickerRatesRepository.deleteAll();
		for (TickerRate rate : rates) {
			tickerRatesRepository.save(rate);
		}
	}

	@Scheduled(fixedDelay=86400000)
	public void refreshFeePredictions() throws JsonProcessingException {
		List<FeeRatePrediction> predictions = fetchFeePredictionsInternal();
		feePredictionRepository.deleteAll();
		for (FeeRatePrediction prediction : predictions) {
			feePredictionRepository.save(prediction);
		}
	}

	@Scheduled(fixedDelay=86400000)
	public void refreshFeeRecommendation() throws JsonProcessingException {
		FeeRate rate = fetchFeesInternal();
		feeRatesRepository.deleteAll();
		feeRatesRepository.save(rate);
	}

	@Scheduled(fixedDelay=54000000) // 15 mins
	public void refreshBinanceRates() throws JsonProcessingException {
		List<BinanceRate> rates = fetchBinanceRatesInternal();
		for (BinanceRate rate : rates) {
			if (rate.getSymbol().indexOf("STX") > -1 || rate.getSymbol().indexOf("ETHBTC") > -1) {
				binanceRatesRepository.save(rate);
			}
		}
	}

	@Scheduled(fixedDelay=54000000)
	public void pushData() throws JsonProcessingException {
		simpMessagingTemplate.convertAndSend("/queue/rates-news", allRates());
	}

	@GetMapping(value = "/v1/rates-news")
	public Map<String, Object> allRates() throws JsonProcessingException {
		Map<String, Object> results = new HashMap<String, Object>();
		results.put("fees", fees());
		results.put("feePredictions", feePredictions());
		results.put("tickerRates", tickerRates(3000));
		results.put("binanceRates", binanceRates(3000));
		return results;
	}

	@GetMapping(value = "/v1/rates/fees")
	public List<FeeRate> fees() throws JsonMappingException, JsonProcessingException {
		List<FeeRate> rates = feeRatesRepository.findAll();
		return rates;
	}

	@GetMapping(value = "/v1/rates/fee-predictions")
	public List<FeeRatePrediction> feePredictions() throws JsonMappingException, JsonProcessingException {
		List<FeeRatePrediction> rates = feePredictionRepository.findAll();
		return rates;
	}

	@GetMapping(value = "/v1/rates/ticker")
	public List<TickerRate> tickerRates() throws JsonMappingException, JsonProcessingException {
		List<TickerRate> rates = ratesService.findTickerRatesByCloseTime(null);
		return rates;
	}

	@GetMapping(value = "/v1/rates/ticker/{limit}")
	public List<TickerRate> tickerRates(@PathVariable Integer limit) throws JsonMappingException, JsonProcessingException {
		List<TickerRate> rates = ratesService.findTickerRatesByCloseTime(limit);
		return rates;
	}

	@GetMapping(value = "/v1/rates/binance")
	public List<BinanceRate> binanceRates() throws JsonProcessingException {
		List<BinanceRate> rates = ratesService.findBinanceRatesByCloseTime(null);
		return rates;
	}

	@GetMapping(value = "/v1/rates/binance/{limit}")
	public List<BinanceRate> binanceRates(@PathVariable Integer limit) throws JsonProcessingException {
		List<BinanceRate> rates = ratesService.findBinanceRatesByCloseTime(limit);
		return rates;
	}

	@GetMapping(value = "/v1/rates/ticker-latest")
	public TickerRate fetchLatestTickerRates() throws JsonProcessingException {
		TickerRate rate = ratesService.findLatestTickerRate();
		return rate;
	}

	@GetMapping(value = "/v1/rates/binance-latest")
	public BinanceRate fetchLatestBinanceRates() throws JsonProcessingException {
		BinanceRate rate = ratesService.findLatestBinanceRate();
		return rate;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@GetMapping(value = "/v1/rates")
	public Map<String, List> rates() {
		List<BinanceRate> ratesB = binanceRatesRepository.findAll();
		List<TickerRate> ratesT = tickerRatesRepository.findAll();
		Map<String, List> map = new HashMap();
		map.put("binance", ratesB);
		map.put("ticker", ratesT);
		return map;
	}

//	@GetMapping(value = "/v1/rates/binance/force-refresh")
//	public List<BinanceRate> binanceRatesRefresh() throws JsonProcessingException {
//		refreshRates();
//		return binanceRatesRepository.findAll();
//	}

	@GetMapping(value = "/v1/rates/ticker/force-refresh")
	public List<TickerRate> tickerRatesRefresh() throws JsonProcessingException {
		refreshTickerRates();
		return tickerRatesRepository.findAll();
	}

	private FeeRate fetchFeesInternal() throws JsonMappingException, JsonProcessingException {
		HttpEntity<String> e = new HttpEntity<String>(getHeaders());
		ResponseEntity<String> response = restTemplate.exchange(feeRatePath, HttpMethod.GET, e, String.class);
		String jsonResp = response.getBody();
		FeeRate feeRate = mapper.readValue(jsonResp, new TypeReference<FeeRate>() {});
		return feeRate;
	}

	private List<TickerRate> fetchTickerRatesInternal() throws JsonMappingException, JsonProcessingException {
		HttpEntity<String> e = new HttpEntity<String>(getHeaders());
		ResponseEntity<String> response = restTemplate.exchange(tickerRatePath, HttpMethod.GET, e, String.class);
		String jsonResp = response.getBody();
		Map<String, TickerRate> rates = mapper.readValue(jsonResp, new TypeReference<Map<String, TickerRate>>() {});
		List<TickerRate> tickers = new ArrayList<TickerRate>();
		for (String currency : rates.keySet()) {
			TickerRate tr = rates.get(currency);
			tr.setCurrency(currency);
			tickers.add(tr);
		}
		return tickers;
	}

	private List<FeeRatePrediction> fetchFeePredictionsInternal() throws JsonMappingException, JsonProcessingException {
		HttpEntity<String> e = new HttpEntity<String>(getHeaders());
		ResponseEntity<String> response = restTemplate.exchange(feePredictionPath, HttpMethod.GET, e, String.class);
		String jsonResp = response.getBody();
		Map<String, List<FeeRatePrediction>> rates = mapper.readValue(jsonResp, new TypeReference<Map<String, List<FeeRatePrediction>>>() {});
		List<FeeRatePrediction> predictions = rates.get("fees");
		return predictions;
	}

	private List<BinanceRate> fetchBinanceRatesInternal() throws JsonMappingException, JsonProcessingException {
		HttpEntity<String> e = new HttpEntity<String>(getHeaders());
		ResponseEntity<String> response = restTemplate.exchange(binanceRatePath, HttpMethod.GET, e, String.class);
		String jsonResp = response.getBody();
		List<BinanceRate> rates = Arrays.asList(mapper.readValue(jsonResp, BinanceRate[].class));
		return rates;
	}

	private HttpHeaders getHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return headers;
	}
}
