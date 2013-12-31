package ph.txtdis.windows;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

public abstract class View {

	private Image iconImage;
	protected Shell shell;

	public View() {
		shell = new Shell(UI.DISPLAY);
		iconImage = new Image(UI.DISPLAY, this.getClass().getResourceAsStream("images/icon.png"));
		shell.setText("txtDIS " + DIS.CLIENT_VERSION);
		shell.setImage(iconImage);
		shell.setLayout(new GridLayout(1, false));
	}

	protected void show() {
		centerShell();
		shell.open();
		while (!shell.isDisposed()) {
			if (!UI.DISPLAY.readAndDispatch())
				UI.DISPLAY.sleep();
		}
		iconImage.dispose();
	}

	protected void centerShell() {
	    shell.pack();
		Monitor primary = shell.getMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = shell.getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		if (bounds.height - 70 < rect.height)
			y = 0;
		if (bounds.width < rect.width)
			x = 0;
		shell.setLocation(x, y);
    }

	protected Shell getShell() {
		return shell;
	}
}