package com.radicle.payments.payments.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.radicle.payments.common.ApiHelper;
import com.radicle.payments.common.Principal;
import com.radicle.payments.payments.api.model.TransactionFromApiBean;
import com.radicle.payments.payments.api.model.TransactionIdBean;
import com.radicle.payments.payments.service.OpenNodePaymentRepository;
import com.radicle.payments.payments.service.SquarePaymentRepository;
import com.radicle.payments.payments.service.domain.OpenNodePayment;
import com.radicle.payments.payments.service.domain.SquarePayment;

@Configuration
@EnableAsync
@EnableScheduling
public class PaymentWatcher {

	@Autowired private SquarePaymentRepository squarePaymentRepository;
	@Autowired private OpenNodePaymentRepository openNodePaymentRepository;
	@Autowired ApiHelper apiHelper;
	@Autowired private ObjectMapper mapper;

	@Scheduled(fixedDelay=30000)
	public void watchForStaxTransfers() throws JsonProcessingException {
		resolveSquarePayments();
		resolveOpenNodePayments();
	}
	
	private void resolveSquarePayments() throws JsonProcessingException {
		List<SquarePayment> payments = squarePaymentRepository.findByTransactionDataTxStatus("pending");
		if (payments != null) {
			for (SquarePayment payment : payments) {
				String txId = extractTxId(payment.getTransactionData().getStacksmateResponse());
				TransactionFromApiBean tx = checkTx(txId);
				payment.getTransactionData().setTxStatus(tx.getTx_status());
				squarePaymentRepository.save(payment);
			}
		}
	}

	private void resolveOpenNodePayments() throws JsonProcessingException {
		List<OpenNodePayment> payments = openNodePaymentRepository.findByTransactionDataTxStatus("pending");
		if (payments != null) {
			for (OpenNodePayment payment : payments) {
				String txId = extractTxId(payment.getTransactionData().getStacksmateResponse());
				TransactionFromApiBean tx = checkTx(txId);
				payment.getTransactionData().setTxStatus(tx.getTx_status());
				openNodePaymentRepository.save(payment);
			}
		}
	}

	private TransactionFromApiBean checkTx(String txId) throws JsonProcessingException {
		TransactionFromApiBean pox = null;
		if (txId != null) {
			Principal path = new Principal("GET", "/extended/v1/tx/" + txId, null);
			String json = apiHelper.fetchFromApi(path);
			pox = (TransactionFromApiBean)mapper.readValue(json, new TypeReference<TransactionFromApiBean>() {});
		}
		return pox;
	}

	private String extractTxId(String stacksmateResponse) {
		TransactionIdBean txidBean;
		try {
			txidBean = mapper.readValue(stacksmateResponse, new TypeReference<TransactionIdBean>() {});
			return txidBean.getTxid();
		} catch (Exception e) {
			return null;
		}
	}

}
