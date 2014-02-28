package ph.txtdis.windows;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class InputDialog extends DialogView {
	protected Text text;
	protected String module, input;
	private ArrayList<String> inputs = new ArrayList<>();

	public InputDialog(String module) {
		super(Type.OPEN, "");
		this.module = module;
		display();
	}

	@Override
	protected void setRightPane() {
		super.setRightPane();
		text = new Text(shell, SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_BOTH));
		text.setBackground(UI.YELLOW);
	}

	@Override
	protected void setListener() {
		text.addListener (SWT.DefaultSelection, new Listener () {
			@Override
			public void handleEvent (Event e) {
				setOkButtonAction();
			}
		});
	}


	public ArrayList<String> getInputs() {
	    return inputs;
    }

	public String getInput() {
		return input;
	}

	public void addInput(Text text) {
		String input = text.getText();
		inputs.add(input.trim());
	}

	@Override
    protected void setFocus() {
		text.setFocus();
    }

	@Override
    protected void setOkButtonAction() {
		addInput(text);
		shell.dispose();
    }
}
