package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Text;


public class MultiLineInputDialog extends DialogView {
	private Text text;
	private String reason;
	
	public MultiLineInputDialog(String msg) {
		super();
		setName("Warning");
		setMessage(msg);
		open();
	}

	public String getText() {
		return reason;
	}
	
	@Override
	public void setRightPane() {
		text = new Text(shell, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP | SWT.BORDER);
		text.setText("\n\n");
		text.setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	@Override
    protected void setOkButtonAction() {
		reason = text.getText();
    }
}
