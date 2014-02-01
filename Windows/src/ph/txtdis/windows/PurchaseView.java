package ph.txtdis.windows;

import java.text.ParseException;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Group;

public class PurchaseView extends OrderView {
	private Button purchaseGeneratorButton;
	private Object[] categories;

	public PurchaseView() {
		this(0);
    }

	public PurchaseView(int id) {
		this(new PurchaseData(id));
	}

	public PurchaseView(PurchaseData data) {
		super(data);
		type = Type.PURCHASE;
		proceed();
	}

	@Override
	protected void addHeader() {
		new Header(this, data) {
			@Override
			protected void layButtons() {
				new ImgButton(buttons, Type.NEW, type);
				new ImgButton(buttons, Type.OPEN, view);
				new WizardButton(buttons, data);
				new TargetButton(buttons, data);
				postButton = new ImgButton(buttons, Type.SAVE, view).getButton();
				purchaseGeneratorButton = new ReportGenerationButton(buttons,
						data).getButton();
				new ImporterButton(buttons, module) {
					@Override
					protected void setStrings() {
						categories = new Query().getList("SELECT * FROM purchase_category;");
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
				new PrintingButton(buttons, data, true);
			}
		};
	}

	@Override
    protected void addSubheader() {
		new Subheader(this, (PurchaseData) data) {
			@Override
            protected void setOrderGroup(OrderView view, OrderData data, Group grpInvoice) {
				referenceIdInput = (new TextDisplayBox(grpInvoice, "P/O #", data.getId()).getText());
            }
		};
    }

	@Override
	protected void setFocus() {
		if (id == 0) {
			partnerIdInput.setTouchEnabled(true);
			partnerIdInput.setFocus();
			purchaseGeneratorButton.setEnabled(false);
		} else {
			purchaseGeneratorButton.setFocus();
		}
	}

	public Button getBtnPrinter() {
		return purchaseGeneratorButton;
	}

	public void setBtnPrinter(Button btnPrinter) {
		this.purchaseGeneratorButton = btnPrinter;
	}

	@Override
    public Posting getPosting() {
	    return new OrderPosting((OrderData) data);
    }
}
