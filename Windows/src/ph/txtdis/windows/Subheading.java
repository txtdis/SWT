package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class Subheading {

	public Subheading(Composite parent, Subheaded data) {
		Label subheading = new Label(parent, SWT.CENTER);
		String text = data.getSubheading();
		subheading.setText(text);
		subheading.setFont(UI.BOLD);
		subheading.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
	}
}
