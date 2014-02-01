package ph.txtdis.windows;

import java.math.BigDecimal;

public class DeliveryItemIdInput extends ItemIdInput {

	public DeliveryItemIdInput(OrderView view, OrderData data) {
		super(view, data);
	}

	@Override
	protected boolean isMonetaryInputValid() {

		if (!isNotDealerIncentive()) {
			new ErrorDialog("This transaction\nis not allowed here");
			return false;
		}
		
		if (rowIdx != 0) {
			new ErrorDialog("Monetary transactions must\nbe done separately");
			return false;
		}

		tableItem.setText(OrderView.UOM_COLUMN, DIS.$);
		data.setUom(Type.$);
		BigDecimal total = data.getEnteredTotal();
		BigDecimal price = BigDecimal.ONE;
		boolean isCredit = !DIS.isNegative(total);
		if (Item.isCredit(itemId)) {
			if (!isCredit) {
				new ErrorDialog("Credit items must\nhave positive value");
				tableItem.dispose();
				UI.goToControl(((DeliveryView) view).getEnteredTotalInput());
				return true;
			}
		} else {
			if (isCredit) {
				new ErrorDialog("Non-Credit items must\nhave negative value");
				tableItem.dispose();
				UI.goToControl(((DeliveryView) view).getEnteredTotalInput());
				return true;
			}

			if (isASalaryTransactionButPartnerIsNotEmployee()) {
				new ErrorDialog("Salary-related transactions\n are for employees only");
				return false;
			}
			price = price.negate();
		}
		tableItem.setText(OrderView.PRICE_COLUMN, DIS.formatTo2Places(price));
		tableItem.setText(view.getQtyColumnIdx(), DIS.formatTo2Places(total.abs()));
		tableItem.setText(OrderView.TOTAL_COLUMN, DIS.formatTo2Places(total));
		view.getComputedTotalDisplay().setText(DIS.formatTo2Places(total));
		postButton.setEnabled(true);
		postButton.setFocus();
		return true;
	}

	protected boolean isNotDealerIncentive() {
		return !Item.isInvoiceOnly(itemId);
	}

	private boolean isASalaryTransactionButPartnerIsNotEmployee() {
		int customerId = data.getPartnerId();
		boolean isAnEmployee = Channel.get(customerId).equals("EMPLOYEE");
		boolean isASalaryTransaction = itemId == DIS.SALARY_DEDUCTION || itemId == DIS.SALARY_CREDIT;
		return (isASalaryTransaction && !isAnEmployee);
	}

	@Override
	protected boolean isItemDiscountSameAsFromSameDayOrders(Type type) {
		return false;
	}

	@Override
	protected boolean isItemDiscountSameAsPrevious() {
		return true;
	}
}