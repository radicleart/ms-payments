package com.radicle.payments.payments.service.domain;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Document
public class SquarePayment {

	@Id private String id;
	private String createdAt;
	private String receiptUrl;
	private String nonce;
	private String locationId;
	private String idempotencyKey;
	private Long amountFiat;
	private String currency;
	private String status;
	private List<String> errors;
	private TransactionData transactionData;
}
