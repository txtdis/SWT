package ph.txtdis.windows;

import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

public class DeliveryView extends OrderView {
	protected Text enteredTotalInput, referenceIdInput;

	public DeliveryView() {
		this(0);
	}

	public DeliveryView(int id) {
		this(new DeliveryData(id));
	}

	public DeliveryView(OrderData data) {
		super(data);
		if (!(data instanceof DeliveryData)) {
			((InputData) this.data).setId(0);
			data.setDate(DIS.TOMORROW);}
		proceed();
	}

	@Override
    protected void addSubheader() {
		new Subheader(this, (DeliveryData) data){
			@Override
            protected void setOrderGroup(OrderView view, OrderData data, Group grpInvoice) {
				referenceIdInput = new TextInputBox(grpInvoice, "S/O #", data.getReferenceId()).getText();
				new TextDisplayBox(grpInvoice, "D/R #", data.getId()).getText();
				enteredTotalInput = new TextInputBox(grpInvoice, "D/R AMT", data.getEnteredTotal()).getText();
            }
		};
    }

	@Override
    protected void addListener() {
		new EnteredTotalAmountInputListener(this, (DeliveryData) data);
		new ReferenceIdEntry(this, (OrderData) data);
	    super.addListener();
    }

	@Override
    protected void setFocus() {
		enteredTotalInput.setTouchEnabled(true);
		enteredTotalInput.setFocus();
    }

	public Text getEnteredTotalInput() {
		return enteredTotalInput;
	}

	public Text getReferenceIdInput() {
		return referenceIdInput;
	}

	@Override
    public Posting getPosting() {
	    return new DeliveryPosting((OrderData) data);
    }
}
