package com.radicle.payments.payments.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.radicle.payments.payments.api.model.ProjectPaymentTotals;
import com.radicle.payments.payments.service.domain.Payment;

@Service
public class PaymentServiceImpl implements PaymentService {

	@Autowired
	private PaymentRepository paymentRepository;

	public ProjectPaymentTotals getProjectPaymentTotals(String projectId) {
		ProjectPaymentTotals ppt = new ProjectPaymentTotals();
		ppt.init(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L);
		List<Payment> payments = paymentRepository.findByProjectId(projectId);
		for (Payment payment : payments) {
			if (payment != null && payment.getAmountMoney() != null && payment.getAmountMoney().getAmount() != null) {
				if (payment.getPaymentType().equals("square")) {
					ppt.setFiatCount(ppt.getFiatCount() + 1);
					ppt.setFiatTotal(ppt.getFiatTotal() + payment.getAmountMoney().getAmount());
				} else if (payment.getPaymentType().equals("paypal")) {
					ppt.setPaypalCount(ppt.getPaypalCount() + 1);
					ppt.setPaypalTotal(ppt.getPaypalTotal() + payment.getAmountMoney().getAmount());
				} else if (payment.getPaymentType().equals("stacks")) {
					ppt.setStacksCount(ppt.getStacksCount() + 1);
					if (payment.getAmountMoney().getAmountStx() != null) {
						Long stacksTotal = (long) (payment.getAmountMoney().getAmountStx() * 1000000.0F);
						ppt.setStacksTotal(ppt.getStacksTotal() + stacksTotal);
					}
					ppt.setStacksFiatTotal(ppt.getStacksFiatTotal() + payment.getAmountMoney().getAmount());
				} else if (payment.getPaymentType().equals("opennode")) {
					ppt.setOpennodeCount(ppt.getOpennodeCount() + 1);
					ppt.setOpennodeTotal(ppt.getOpennodeTotal() + payment.getAmountMoney().getAmount());
				}
			}
		}
 		return ppt;
	}
}