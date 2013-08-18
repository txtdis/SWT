package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

public class Grp {
	private Group group;

	public Grp(Composite parent, int columnCount, String text) {
		group = new Group(parent, SWT.NONE);
		group.setText(text);
		group.setFont(DIS.MONO);
		group.setLayout(new GridLayout(columnCount, false));
	}

	public Grp(Composite parent, int columnCount, String text, int style) {
		this(parent, columnCount, text);
		group.setLayoutData(new GridData(style));		
	}

	public Grp(Composite parent, int columnCount, String text, int horizontalStyle, int verticalStyle,
	        boolean isHorizontalExcessTaken, boolean isVerticalExcessTaken, int horizontalSpan, int verticalSpan) {
		this(parent, columnCount, text);
		group.setLayoutData(new GridData(horizontalStyle, verticalStyle, isHorizontalExcessTaken,
		        isVerticalExcessTaken, horizontalSpan, verticalSpan));
	}

	public Group getGroup() {
		return group;
	}
}
