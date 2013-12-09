package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class DialogView extends View {
	protected String name, message;
	protected Composite header, right, left, footer;
	protected Label image;
	protected Button btnOK, btnCancel;
	protected boolean isCancelled;

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
		setFooter();
		setListener();
		setFocus();
		show();
	}

	@Override
	protected Shell getShell() {
		return new Shell(UI.DISPLAY, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.ON_TOP);
	}

	protected void setHeader() {
		header = new Compo(shell, 2, SWT.FILL, SWT.FILL, true, true, 1, 1).getComposite();
		left = new Compo(header, 1, GridData.FILL_BOTH).getComposite();
		right = new Compo(header, 1, GridData.FILL_BOTH).getComposite();
	}

	protected void setLeftPane() {
		image = new Label(left, SWT.CENTER);
		image.setImage(new Image(UI.DISPLAY, this.getClass().getResourceAsStream(
		        "images/" + name.replace(" ", "").replace("/", "") + "64.png")));
		image.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				image.getImage().dispose();
			}
		});
		image.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));

	}

	protected void setRightPane() {
		Label label = new Label(right, SWT.CENTER);
		label.setText(message);
		label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
	}

	protected void setFooter() {
		footer = new Compo(shell, 2, SWT.FILL, SWT.FILL, true, true, 1, 1).getComposite();
		footer.setLayout(new FillLayout());
		setButton();
	}

	protected void setButton() {
		btnOK = new Button(footer, SWT.PUSH);
		btnOK.setText("OK");
		btnOK.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				setOkButtonAction();
				if (!shell.isDisposed()) {
					image.dispose();
					shell.dispose();
				}
			}
		});
		btnOK.setFocus();
		setCancelButton();
	}

	protected void setCancelButton() {
		btnCancel = new Button(footer, SWT.PUSH);
		btnCancel.setText("Cancel");
		btnCancel.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				isCancelled = true;
				if (image != null)
					image.dispose();
				shell.dispose();
			}
		});
	}

	protected void setOkButtonAction() {
	}

	protected void setListener() {
	};

	protected void setFocus() {
	};

	public void setName(String name) {
		this.name = name;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
