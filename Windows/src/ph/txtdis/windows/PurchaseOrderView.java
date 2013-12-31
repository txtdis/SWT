package ph.txtdis.windows;

import java.text.ParseException;

import org.eclipse.swt.widgets.Button;

public class PurchaseOrderView extends OrderView {
	protected PurchaseOrder purchaseOrder;
	private Button btnPOGenerator;
	private Object[] categories;

	public PurchaseOrderView(int orderId, String bizUnit,
			Boolean isUomOrDayBased, Integer uomOrDayCount) {
		super(orderId, bizUnit, isUomOrDayBased, uomOrDayCount);
	}

	public PurchaseOrderView(int orderId) {
		super(orderId);
	}

	@Override
	protected void setTitleBar() {
		new ReportTitleBar(this, purchaseOrder) {
			@Override
			protected void layButtons() {
				new NewButton(buttons, module);
				new RetrieveButton(buttons, report);
				new WizardButton(buttons, report);
				new TargetButton(buttons, report);
				postButton = new PostButton(buttons, order)
						.getButton();
				btnPOGenerator = new ReportGenerationButton(buttons,
						purchaseOrder).getButton();
				new ImportButton(buttons, module) {
					@Override
					protected void setStrings() {
						categories = new Data().getData("SELECT * FROM purchase_category;");
						prefix = new String[12];
						msg = new String[12];
						for (int i = 0; i < prefix.length; i++) {
							prefix[i] = "GT " + categories[i] + " TEMPLATE";
							msg[i] = "Import new " + categories[i]
									+ " P/O Template";
						}
						info = "Purchase Order Templates ";
					}

					@Override
					protected void setModule(int i) {
						module = "Purchase Order - " + categories[i];
					}

					@Override
					protected void setDate(String fileName, String prefix)
							throws ParseException {
						date = DIS.TODAY;
					}
				};
				new PrintingButton(buttons, purchaseOrder, true);
				new ExitButton(buttons, module);
			}
		};
	}

	@Override
	protected void runClass() {
		report = order = purchaseOrder = new PurchaseOrder(id, bizUnit,
				isUomOrDayBased, uomOrDayCount);
	}

	@Override
	protected void setFocus() {
		if (id == 0) {
			partnerIdInput.setTouchEnabled(true);
			partnerIdInput.setFocus();
			btnPOGenerator.setEnabled(false);
		} else {
			btnPOGenerator.setFocus();
		}
	}

	public Button getBtnPrinter() {
		return btnPOGenerator;
	}

	public void setBtnPrinter(Button btnPrinter) {
		this.btnPOGenerator = btnPrinter;
	}

	public static void main(String[] args) {
		//Database.getInstance().getConnection("badette","013094","localhost");
		Database.getInstance().getConnection("sheryl", "10-8-91", "mgdc_smis");
		new PurchaseOrderView(0);
		Database.getInstance().closeConnection();
	}

}
