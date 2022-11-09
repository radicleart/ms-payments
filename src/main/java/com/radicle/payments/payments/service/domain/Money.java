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
@TypeAlias(value = "Money")
public class Money {

	private Float amountStx;
	private Long amountBtc;
	private Long amount;
	private String currency;

}
