package com.radicle.payments.payments.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.radicle.payments.payments.service.OpenNodePaymentRepository;
import com.radicle.payments.payments.service.PaymentRepository;
import com.radicle.payments.payments.service.SquarePaymentRepository;
import com.radicle.payments.payments.service.domain.OpenNodePayment;
import com.radicle.payments.payments.service.domain.SquarePayment;

@RestController
public class PaymentController {

	@Autowired private SquarePaymentRepository squarePaymentRepository;
	@Autowired private OpenNodePaymentRepository openNodePaymentRepository;
	@Autowired private PaymentRepository paymentRepository;

	@GetMapping(value = "/v2/purchase/{paymentId}")
	public Object getPurchase(HttpServletRequest request, @PathVariable String paymentId) {
		Optional<OpenNodePayment> p1 = openNodePaymentRepository.findById(paymentId);
		if (p1.isPresent()) {
			return p1.get();
		}
		Optional<SquarePayment> p2 = squarePaymentRepository.findById(paymentId);
		if (p2.isPresent()) {
			return p2.get();
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	@GetMapping(value = "/v2/purchases/{sender}")
	public Map<String, List> getPayments(@PathVariable String sender) throws JsonProcessingException {
		Map<String, List>  payments = new HashMap<String, List>();
		List<OpenNodePayment> onps = openNodePaymentRepository.findBySender(sender);
		List<SquarePayment> sps = squarePaymentRepository.findBySender(sender);
		payments.put("opennode", onps);
		payments.put("square", sps);
		return payments;
	}

	@SuppressWarnings("rawtypes")
	@GetMapping(value = "/v2/purchases/{contractId}/{sender}/{status}")
	public Map<String, List> getPayments(@PathVariable String contractId, @PathVariable String sender, @PathVariable String status) throws JsonProcessingException {
		Map<String, List>  payments = new HashMap<String, List>();
		List<OpenNodePayment> onps = openNodePaymentRepository.findByContractIdAndSenderAndTxStatus(contractId, sender, status);
		List<SquarePayment> sps = squarePaymentRepository.findByContractIdAndSenderAndTxStatus(contractId, sender, status);
		payments.put("opennode", onps);
		payments.put("square", sps);
		return payments;
	}

	@SuppressWarnings("rawtypes")
	@GetMapping(value = "/v2/purchases/{contractId}/{sender}")
	public Map<String, List> getPayments(@PathVariable String contractId, @PathVariable String sender) throws JsonProcessingException {
		Map<String, List>  payments = new HashMap<String, List>();
		List<OpenNodePayment> onps = openNodePaymentRepository.findByContractIdAndSender(contractId, sender);
		List<SquarePayment> sps = squarePaymentRepository.findByContractIdAndSender(contractId, sender);
		payments.put("opennode", onps);
		payments.put("square", sps);
		return payments;
	}

	@GetMapping(value = "/v2/payments/nonces/{stxAddress}")
	public String getNonces(@PathVariable String stxAddress) throws JsonProcessingException {
		String json = ""; //walletService.getNonces(stxAddress);
		return json;
	}

//	@PostMapping(value = "/v2/payment")
//	public Payment post(HttpServletRequest request, @RequestBody Payment payment) {
//		paymentRepository.save(payment);
//		return paymentRepository.save(payment);
//	}
//
//	@PutMapping(value = "/v2/payment")
//	public Payment put(HttpServletRequest request, @RequestBody Payment payment) {
//		return paymentRepository.save(payment);
//	}

}
