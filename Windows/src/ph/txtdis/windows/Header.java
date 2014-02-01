package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public abstract class Header {
	protected ReportView view;
	protected Data data; 
	protected Composite buttons;
	protected String module;

	public Header(ReportView view, Data data) {
		this.view = view;
		this.data = data;
		module = view.getType().getName();

	    Composite bar = new Compo(view.getShell(), 2, SWT.FILL, SWT.BEGINNING, true, true, 1, 1).getComposite();
		setModuleLabel(bar);
		buttons = new Compo(bar, 10, GridData.HORIZONTAL_ALIGN_END).getComposite();
		layButtons();
	}

	private void setModuleLabel(Composite bar) {
		Label lbl = new Label (bar, SWT.NONE);
		lbl.setText(module);
		lbl.setForeground(UI.BLUE);	
		lbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lbl.setFont(UI.BIG);
    }
	
	protected abstract void layButtons();
}
