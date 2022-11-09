package com.radicle.payments.payments.service;

import com.radicle.payments.payments.api.model.ProjectPaymentTotals;

public interface PaymentService
{
	public ProjectPaymentTotals getProjectPaymentTotals(String projectId);
}
