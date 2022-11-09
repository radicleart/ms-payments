package com.radicle.payments.payments.service.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonProperty;

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
@TypeAlias(value = "OpenNodePayment")
@Document
public class OpenNodePayment {

	@Id private String id;
	private String amount;
	private String description;
	private String callback_url;
	private Boolean routeHints;
	private String created_at;
	private String status;
	private String success_url;
	private String hashed_order;
	private String hosted_checkout_url;
	private String currency;
	private String source_fiat_value;
	private String fiat_value;
	private String auto_settle;
	private String notif_email;
	private String address;
	private ChainInvoice chain_invoice;
	private String uri;
	private String ttl;
	private LightningInvoice lightning_invoice;
	private TransactionData transactionData;

	@JsonProperty("route_hints")
	public Boolean getRouteHints() {
	    return routeHints;
	}

}
