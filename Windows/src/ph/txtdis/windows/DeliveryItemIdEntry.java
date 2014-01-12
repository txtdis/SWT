package ph.txtdis.windows;

import java.math.BigDecimal;

public class DeliveryItemIdEntry extends ItemIdInput {

	public DeliveryItemIdEntry(OrderView orderView, Order report) {
		super(orderView, report);
	}

	@Override
	protected boolean isItemMonetaryAndTransactionValid() {
		if (isASalaryTransactionButPartnerIsNotEmployee()) {
			clearTableItemEntry("Salary-related transactions\n are for employees only");
			return false;
		} else if (isDebitTransaction()) {
			if (!isEnteredTotalNegative) {
				clearTableItemEntry("Entered total for\n monetary transactions\n must be negative");
				return false;
			} else {
				order.setAMonetaryTransaction(isDebitTransaction());
				order.setPrice(BigDecimal.ONE.negate());
				return true;
			}
		} else if (isEnteredTotalNegative) {
			clearTableItemEntry("A negative entered total is\n for monetary transactions only");
			return false;
		} else {
			order.setPrice(BigDecimal.ONE);
			return true;
		}
	}

	private boolean isDebitTransaction() {
		return item.isMonetaryType(itemId, order.getType()) && itemId != DIS.SALARY_CREDIT;
	}

	private boolean isASalaryTransactionButPartnerIsNotEmployee() {
		int customerId = order.getPartnerId();
		boolean isAnEmployee = new Customer().isAnEmployee(customerId);
		boolean isASalaryTransaction = itemId == DIS.SALARY_DEDUCTION || itemId == DIS.SALARY_CREDIT;
		return (isASalaryTransaction && !isAnEmployee);
	}

	@Override
	protected boolean isItemDiscountSameAsFromSameDayOrders() {
		return false;
	}

	@Override
	protected boolean isItemDiscountSameAsPrevious() {
		return true;
	}
}