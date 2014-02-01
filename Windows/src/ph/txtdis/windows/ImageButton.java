package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class ImageButton {
	protected Composite parent;
	protected Button button;
	protected String module;
	protected Image image;

	public ImageButton(final Composite parent, String module, String icon, String tooltip) {
		this.parent = parent;
		this.module = module;

		button = new Button(parent, SWT.FLAT);
		image = new Image(parent.getDisplay(), this.getClass().getResourceAsStream("images/" + icon + ".png"));
		setIcon();
		Listener listener = new Listener() {
			@Override
			public void handleEvent(Event e) {
				switch (e.type) {
					case SWT.Selection:
						proceed();
						break;
					case SWT.FocusOut:
						doWhenOutOfFocus();
						break;
					case SWT.Dispose:
						button.getImage().dispose();
						break;
				}
			}
		};
		button.setToolTipText(tooltip);
		button.addListener(SWT.Selection, listener);
		button.addListener(SWT.Dispose, listener);
		button.addListener(SWT.FocusOut, listener);
	}

	protected void setIcon() {
		button.setImage(image);
	}

	protected void proceed() {
	}

	protected void doWhenOutOfFocus() {
	}

	public Button getButton() {
		return button;
	}
}
