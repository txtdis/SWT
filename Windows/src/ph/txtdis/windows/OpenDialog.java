package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class OpenDialog extends DialogView {
	private Text idInput;
	private Type moduleType;
	private Shell parentShell;

	public OpenDialog(Type moduleType, Shell parentShell) {
		super(Type.OPEN, "Enter\n" + moduleType.getName().replace(" ", "\n") + " #");
		this.moduleType = moduleType;
		this.parentShell = parentShell;
		display();
	}

	@Override
	public void setRightPane() {
		super.setRightPane();
		setControl();
	}

	private void setControl() {
		idInput = new TextInputBox(right, "", 0).getText();
		idInput.addListener(SWT.DefaultSelection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				setOkButtonAction();
			}
		});
	}

	@Override
	protected void setOkButtonAction() {
		String textId = idInput.getText();
		int id = DIS.parseInt(textId);

		if (!OrderControl.isOnFile(moduleType, id)) {
			new ErrorDialog(moduleType.getName() + " #" + id + "\nis not in our system.");
			idInput.setText("");
			idInput.setFocus();
		} else {
			parentShell.close();
			shell.close();
			String name = DIS.extractClassName(moduleType) + "View";
			Object[] parameters = { id };
			Class<?>[] parameterTypes = { int.class };
			DIS.instantiateClass(name, parameters, parameterTypes);
		}
	}

	@Override
	protected void setFocus() {
		idInput.setFocus();
	}
}
