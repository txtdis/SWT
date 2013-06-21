package ph.txtdis.windows;

import org.eclipse.swt.widgets.Composite;


public class DeleteButton extends ImageButton {

	public DeleteButton(Composite parent, String module) {
		super(parent, module, "Cancel", "Delete an Open " + module);
	}
	
	@Override
	public void open(){
		parent.getShell().close();
	}
}
