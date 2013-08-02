package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class Compo {
	Composite composite;
	
	public Compo(Composite parent, int columnCount, int style) {
		composite = new Composite(parent, SWT.NO_TRIM);
		composite.setLayout(new GridLayout(columnCount, false));
		composite.setLayoutData(new GridData(style));
	}

	public Composite getComposite() {
		return composite;
	}
}
