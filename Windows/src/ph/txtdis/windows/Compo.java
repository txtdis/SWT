package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class Compo {
	Composite composite;

	public Compo(Composite parent, int columnCount) {
		composite = new Composite(parent, SWT.NO_TRIM);
		composite.setLayout(new GridLayout(columnCount, false));
	}

	public Compo(Composite parent, int columnCount, int style) {
		this(parent, columnCount);
		composite.setLayoutData(new GridData(style));
	}

	public Compo(Composite parent, int columnCount, int horizontalStyle, int verticalStyle,
	        boolean isHorizontalExcessTaken, boolean isVerticalExcessTaken, int horizontalSpan, int verticalSpan) {
		this(parent, columnCount);
		composite.setLayoutData(new GridData(horizontalStyle, verticalStyle, isHorizontalExcessTaken,
		        isVerticalExcessTaken, horizontalSpan, verticalSpan));
	}

	public Composite getComposite() {
		return composite;
	}
}
