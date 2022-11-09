package com.radicle.payments.payments.api;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.radicle.payments.payments.service.PaymentHelper;
import com.radicle.payments.payments.service.SquarePaymentRepository;
import com.radicle.payments.payments.service.domain.SquarePayment;
import com.squareup.square.SquareClient;
import com.squareup.square.api.PaymentsApi;
import com.squareup.square.models.CreatePaymentRequest;
import com.squareup.square.models.CreatePaymentResponse;
import com.squareup.square.models.Money;

@RestController
public class SquareStacksMateController {

    private static final Logger logger = LogManager.getLogger(SquareStacksMateController.class);
	@Autowired private SquareClient squareStacksMateClient;
	@Autowired private Environment environment;
	@Autowired private SquarePaymentRepository squarePaymentRepository;
	@Autowired private PaymentHelper paymentHelper;
	@Autowired private SimpMessagingTemplate simpMessagingTemplate;

	@PostMapping(value = "/v2/stacksmate/square/charge")
	public SquarePayment charge(@RequestBody SquarePayment paymentRequest) throws Exception {
		try {
			logger.info("PAYMENTS-SquareSM: Creating Charge", squareStacksMateClient);
			String key = environment.getProperty("SQUARE_SM_LOCATION_ID");
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
			    .locationId(key)
			    .build();
			PaymentsApi paymentsApi = squareStacksMateClient.getPaymentsApi();
			CreatePaymentResponse cpr = paymentsApi.createPayment(body);
			paymentRequest.setId(cpr.getPayment().getId());
			if (cpr.getErrors() == null || cpr.getErrors().size() == 0) {
				paymentRequest.setCreatedAt(cpr.getPayment().getCreatedAt());
				paymentRequest.setReceiptUrl(cpr.getPayment().getReceiptUrl());
				paymentRequest.setId(cpr.getPayment().getId());
				paymentRequest.setStatus("paid");
				logger.info("PAYMENTS-SquareSM: sending stacks tx");
				paymentRequest = sendStacksTx(paymentRequest);
			} else {
				logger.info("PAYMENTS-Square: Errors" + cpr.getErrors());
				paymentRequest.setStatus("errored");
				List<String> errors = new ArrayList<String>();
				for (com.squareup.square.models.Error e : cpr.getErrors()) {
					errors.add(e.getCode() + " " + e.getField() + " " + e.getDetail());
					logger.info("PAYMENTS-Square: Error Code" + e.getCode());
					logger.info("PAYMENTS-Square: Error Field" + e.getField());
					logger.info("PAYMENTS-Square: Error Category" + e.getCategory());
					logger.info("PAYMENTS-Square: Error Detail" + e.getDetail());
				}
				paymentRequest.setErrors(errors);
			}
		} catch (Exception e) {
			paymentRequest.getTransactionData().setTxStatus("errored");
			List<String> errors = new ArrayList<String>();
			errors.add(e.getMessage());
			paymentRequest.setErrors(errors);
			throw e;
		}
		squarePaymentRepository.save(paymentRequest);
		return paymentRequest;
	}
	
	private SquarePayment sendStacksTx(SquarePayment payment) throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, UnsupportedEncodingException {
		String hash = paymentHelper.createHash(payment.getId());
		payment.getTransactionData().setHashed_order(hash);
		payment.getTransactionData().setPaymentId(payment.getId());
		payment.getTransactionData().setTxStatus("started");
		// save it here in case we need to do a refund later
		squarePaymentRepository.save(payment);
		String response = null;
		try {
			response = paymentHelper.sendPackage(payment.getTransactionData());
			if (response == null || response.indexOf("error") > -1) {
				payment.getTransactionData().setTxStatus("errored");
			} else {
				payment.getTransactionData().setTxStatus("pending");
			}
			payment.getTransactionData().setStacksmateResponse(response);
			squarePaymentRepository.save(payment);
		} catch (Exception e) {
			payment.getTransactionData().setTxStatus("errored");
			squarePaymentRepository.save(payment);
		}
		simpMessagingTemplate.convertAndSend("/queue/payment-news-" + payment.getId(), response);
		return payment;
	}
}
