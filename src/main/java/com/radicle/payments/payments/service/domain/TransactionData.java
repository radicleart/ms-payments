package com.radicle.payments.payments.service.domain;

import org.springframework.data.annotation.TypeAlias;

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
@TypeAlias(value = "TransactionData")
public class TransactionData {

	private String txStatus;
	private String hashed_order;
	private String email;
	private String currency;
	private Long price;
	private Long amount;
	private Float amountFiat;
	private Float amountBtc;
	private Float amountStx;
	private Long batchOption;
	private Long nftIndex;
	private String paymentId;
	private String type;
	private String contractId;
	private String tokenContractAddress;
	private String tokenContractName;
	private String sender;
	private String recipient;
	private String owner;
	private String assetName;
	private String stacksmateResponse;

}
