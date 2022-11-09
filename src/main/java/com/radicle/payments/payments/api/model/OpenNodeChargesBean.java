package com.radicle.payments.payments.api.model;

import org.springframework.data.annotation.TypeAlias;

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
@TypeAlias(value = "OpenNodeChargesBean")
public class OpenNodeChargesBean {

	private String amount;
	private String description;
	private String callback_url;
	private Boolean routeHints;

	@JsonProperty("route_hints")
	public Boolean getRouteHints() {
	    return routeHints;
	}

}
