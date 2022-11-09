package com.radicle.payments.payments.api.model;

import org.springframework.data.annotation.TypeAlias;

import com.radicle.payments.payments.service.domain.OpenNodePayment;

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
@TypeAlias(value = "OpenNodeChargesResponse")
public class OpenNodeChargesResponse {

	private OpenNodePayment data;

}
