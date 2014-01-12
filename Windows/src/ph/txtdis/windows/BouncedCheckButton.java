package ph.txtdis.windows;

import org.eclipse.swt.widgets.Composite;

public class BouncedCheckButton extends ReportButton {
	private Remittance remit;

	public BouncedCheckButton(Composite parent, Remittance remit) {
		super(parent, remit, "Cancel", "Tag check payment\nhas bounced");
	}

	@Override
	protected void doWhenSelected() {
		remit = (Remittance) report;
		int partnerId = remit.getPartnerId();
		String name = new Customer().getName(partnerId);
		new DialogView("Cancel", "You are about to cancel\n" + name + "\nCheck #" + remit.getReferenceId()) {

			@Override
			protected void setOkButtonAction() {
				super.setOkButtonAction();
				if (new RemittanceCancellationPosting().set(remit)) {
					new RemittanceView(new Remittance(remit.getId()));
				}
			}
		};
	}
}
