package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;


public class DialogView extends View {	
	protected String name, message;
	protected Composite header;
	protected Label image;
	protected Button btnOK, btnCancel;
	private Composite footer;

	public DialogView() {
		super();
	}

	public DialogView(String name, String message) {
		super();
		this.name = name;
		this.message = message;
	}

	public void open() {
		shell.setText(name);
		setHeader();
		setLeftPane();
		setRightPane();
		// Button/s at Footer
		footer = new Composite(shell, SWT.END);
		footer.setLayoutData(new GridData(
				GridData.FILL, GridData.CENTER, true, true));
		footer.setLayout(new FillLayout());
		setButton();
		setListener();
		setFocus();
		show();
	}
	
	@Override
	protected void setShell() {
		shell = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.ON_TOP);
	}
	
	protected void setHeader() {
		header = new Composite(shell, SWT.FILL);
		header.setLayout(new GridLayout(2, false));
		header.setLayoutData(new GridData(
				GridData.FILL, GridData.FILL, true, true));
	}
	
	protected void setLeftPane() {
		image = new Label(header, SWT.CENTER);
		image.setImage(new Image(display, this.getClass().getResourceAsStream(
				"images/" + name.replace(" ","").replace("/", "") + "64.png")));
		image.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				image.getImage().dispose();				
			}
		});
		image.setLayoutData(new GridData(
				GridData.CENTER, GridData.CENTER, true, true, 1, 4));
		
	}

	protected void setRightPane() {
		Label label = new Label(header, SWT.CENTER);
		label.setText(message);
	}

	protected void setButton() {
		btnOK = new Button(footer, SWT.PUSH);
		btnOK.setText("OK");
		btnOK.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				setOkButtonAction();
			}
		});
		btnOK.setFocus();
	}
	
	protected void setCancelButton() {
		btnCancel = new Button(getFooter(), SWT.PUSH);
		btnCancel.setText("Cancel");
		btnCancel.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				shell.dispose();
			}
		});
	}
	
	protected void setOkButtonAction() {
		image.dispose();
		shell.close();
	}
	
	protected void setListener() {};
	protected void setFocus() {};

	public void setName(String name) {
		this.name = name;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Composite getFooter() {
		return footer;
	}

}
