package ph.txtdis.windows;

import org.eclipse.swt.widgets.Composite;

public class RemitImportButton extends ImageButton {

	public RemitImportButton(Composite parent, String module) {
		super(parent, module, "Download", "Import undeposited remits");
	}

	@Override
	protected void proceed() {
		new RemitView(new RemitData(null));
	}
}