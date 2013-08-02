package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ModuleTitleBar {
	protected Composite buttons;
	protected View view;
	protected String module;

	public ModuleTitleBar(View view, String module) {
		this.view = view;
		this.module = module;

		Composite cmpBar = new Composite(view.getShell(), SWT.NO_TRIM);
		cmpBar.setLayout(new GridLayout(2, false));
		GridData gdBar = new GridData();
		gdBar.horizontalAlignment = GridData.FILL;
		gdBar.verticalAlignment = GridData.BEGINNING;
		gdBar.grabExcessHorizontalSpace = true;
		cmpBar.setLayoutData(gdBar);

		Label lbl = new Label (cmpBar, SWT.NONE);
		lbl.setText(module);
		lbl.setForeground(DIS.BLUE);	
		lbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lbl.setFont(DIS.BIG);

		buttons = new Composite(cmpBar, SWT.NO_TRIM);
		buttons.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		buttons.setLayout(new RowLayout());	
	}
	
	protected void layButtons() {
		new ExitButton(buttons, module).getButton();
	}
}
