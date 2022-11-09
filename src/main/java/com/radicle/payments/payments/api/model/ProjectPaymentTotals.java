package com.radicle.payments.payments.api.model;

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
@TypeAlias(value = "NftPurchaseOrder")
public class ProjectPaymentTotals {

	private Long stacksCount;
	private Long stacksFiatTotal;
	private Long fiatCount;
	private Long opennodeCount;
	private Long paypalCount;
	private Long stacksTotal;
	private Long fiatTotal;
	private Long opennodeFiatTotal;
	private Long opennodeTotal;
	private Long paypalTotal;
	
	public void init(Long stacksCount, Long stacksFiatTotal, 
			Long fiatCount, Long opennodeCount,Long paypalCount, 
			Long stacksTotal, Long fiatTotal, Long opennodeTotal, 
			Long paypalTotal, Long opennodeFiatTotal) {
		this.stacksCount = stacksCount;
		this.stacksFiatTotal = stacksFiatTotal;
		this.fiatCount = fiatCount;
		this.opennodeCount = opennodeCount;
		this.paypalCount = paypalCount;
		this.stacksTotal = stacksTotal;
		this.fiatTotal = fiatTotal;
		this.opennodeTotal = opennodeTotal;
		this.paypalTotal = paypalTotal;
		this.opennodeFiatTotal = opennodeFiatTotal;
	}

}
