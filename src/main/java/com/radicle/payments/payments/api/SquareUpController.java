package com.radicle.payments.payments.api;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.radicle.payments.payments.service.SquarePaymentRepository;
import com.radicle.payments.payments.service.domain.SquarePayment;
import com.squareup.square.SquareClient;
import com.squareup.square.api.PaymentsApi;
import com.squareup.square.exceptions.ApiException;
import com.squareup.square.models.CreatePaymentRequest;
import com.squareup.square.models.CreatePaymentResponse;
import com.squareup.square.models.Money;

@RestController
public class SquareUpController {

    private static final Logger logger = LogManager.getLogger(SquareUpController.class);
	@Autowired private SquareClient squareClient;
	@Autowired private ObjectMapper mapper;
	@Autowired private Environment environment;
	@Autowired private SquarePaymentRepository squarePaymentRepository;

	@GetMapping(value = "/oauth-redirect")
	public String fetchPayment(HttpServletRequest request) {
		return "success";
	}

	@PostMapping(value = "/v1/square/charge")
	public CreatePaymentResponse charge(@RequestBody SquarePayment paymentRequest) throws ApiException, IOException {
		logger.info("PAYMENTS-Square: Creating Charge", squareClient);
		Money bodyAmountMoney = new Money.Builder()
			    .amount(paymentRequest.getAmountFiat())
			    .currency(paymentRequest.getCurrency())
			    .amount(paymentRequest.getAmountFiat())
			    .build();
		CreatePaymentRequest body = new CreatePaymentRequest.Builder(
				paymentRequest.getNonce(),
				paymentRequest.getIdempotencyKey(),
		        bodyAmountMoney)
		    .autocomplete(true)
		    .locationId(environment.getProperty("SQUARE_LOCATION_ID"))
		    .build();
		PaymentsApi paymentsApi = squareClient.getPaymentsApi();
		CreatePaymentResponse cpr = paymentsApi.createPayment(body);
		if (cpr.getErrors() == null || cpr.getErrors().size() == 0) {
			squarePaymentRepository.save(paymentRequest);
			return cpr;
		}
		for (com.squareup.square.models.Error e : cpr.getErrors()) {
			logger.info("PAYMENTS-Square: Error Code" + e.getCode());
			logger.info("PAYMENTS-Square: Error Field" + e.getField());
			logger.info("PAYMENTS-Square: Error Category" + e.getCategory());
			logger.info("PAYMENTS-Square: Error Detail" + e.getDetail());
		}
		return cpr;
	}
}
