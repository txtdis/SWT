package ph.txtdis.windows;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

public class FocusButton extends ImageButton {
	
	private Image iconFocus;
	protected Report report;

	public FocusButton(Composite parent, Report report, String icon, String tooltip) {
		super(parent, report.getModule(), icon, tooltip);
		this.report = report;
		iconFocus = new Image(parent.getDisplay(), 
				this.getClass().getResourceAsStream("images/" + icon + "Focus.png"));
	}
	
	@Override
	protected void open() {
		parent.getShell().dispose();
		new CustomerView(0);
	}
	
	@Override
	protected void setIcon() {
		super.setIcon();
		button.addFocusListener(new FocusListener()  {

			@Override
			public void focusGained(FocusEvent e) {
				button.setImage(iconFocus);
			}

			@Override
			public void focusLost(FocusEvent e) {
				button.setImage(image);
			}
		});
	}
}
