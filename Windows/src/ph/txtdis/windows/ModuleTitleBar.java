package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ModuleTitleBar {
	protected Composite buttons;
	protected View view;
	protected String module;

	public ModuleTitleBar(View view, String module) {
		this.view = view;
		this.module = module;

		Composite bar = new Compo(view.getShell(), 2, SWT.FILL, SWT.BEGINNING, true, true, 1, 1).getComposite();
		Label lbl = new Label (bar, SWT.NONE);
		lbl.setText(module);
		lbl.setForeground(UI.BLUE);	
		lbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lbl.setFont(UI.BIG);

		buttons = new Compo(bar, 10, GridData.HORIZONTAL_ALIGN_END).getComposite();
	}
	
	protected void layButtons() {
		new ExitButton(buttons, module).getButton();
	}
}
