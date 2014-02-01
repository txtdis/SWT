package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public abstract class DialogView extends View {
	protected String message;
	protected Type type;
	protected Composite header, right, left, footer;
	protected Label image;
	protected Button btnOK, btnCancel;
	protected boolean isCancelled;

	public DialogView() {
		super();
	}

	public DialogView(Type type, String message) {
		this();
		this.type = type;
		this.message = message;
	}

	@Override
    protected void proceed() {
		setHeader();
		setLeftPane();
		setRightPane();
		setFooter();
		setListener();
		setFocus();
		show();
    }

	@Override
	protected void createShell() {
		shell = new Shell(UI.DISPLAY, SWT.APPLICATION_MODAL | SWT.CLOSE | SWT.ICON);
		shell.setText("txtDIS " + Login.version());
		shell.addListener(SWT.CLOSE, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (image != null)
					image.getImage().dispose();
				shell.close();
			}
		});
	}

	protected void setHeader() {
		header = new Compo(shell, 2, SWT.FILL, SWT.FILL, true, true, 1, 1).getComposite();
		left = new Compo(header, 1, GridData.FILL_BOTH).getComposite();
		right = new Compo(header, 1, GridData.FILL_BOTH).getComposite();
	}

	protected void setLeftPane() {
		image = new Label(left, SWT.CENTER);
		image.setImage(new Image(UI.DISPLAY, this.getClass().getResourceAsStream("buttons/" + type + "64.png")));
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
			}
		});
		btnOK.setFocus();
	}

	protected void setOkButtonAction() {
		shell.close();
	}

	protected void setListener() {
	};

	protected void setFocus() {
	};
}
