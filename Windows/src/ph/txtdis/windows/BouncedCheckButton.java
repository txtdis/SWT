package ph.txtdis.windows;

import org.eclipse.swt.widgets.Composite;

public class BouncedCheckButton extends ReportButton {
	private RemitData remit;

	public BouncedCheckButton(Composite parent, RemitData remit) {
		super(parent, remit, "Cancel", "Tag check payment\nhas bounced");
	}

	@Override
	protected void proceed() {
		remit = (RemitData) data;
		int partnerId = remit.getPartnerId();
		String name = Customer.getName(partnerId);
		new DialogView(Type.CANCEL, "You are about to cancel\n" + name + "\nCheck #" + remit.getReferenceId()) {

			@Override
			protected void setOkButtonAction() {
				super.setOkButtonAction();
				Posting posting = new RemitCancellationPosting(remit);
				posting.save();
				if (posting.isOK())
					new RemitView(new RemitData(remit.getId()));
			}
		};
	}
}
