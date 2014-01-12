package ph.txtdis.windows;

import org.eclipse.swt.widgets.Composite;

public class RemittanceImporterButton extends ImageButton {

	public RemittanceImporterButton(Composite parent, String module) {
		super(parent, module, "Download", "Import undeposited remittances");
	}

	@Override
	protected void doWhenSelected() {
		new RemittanceView(new Remittance(null));
	}
}