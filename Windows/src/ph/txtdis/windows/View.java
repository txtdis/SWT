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
		shell = new Shell(DIS.DISPLAY);
		iconImage = new Image(DIS.DISPLAY, this.getClass().getResourceAsStream("images/icon.png"));
		String version = "0.9." + DIS.BUILD + "." + DIS.DEBUG;
		shell.setLayout(new GridLayout(1, true));
		shell.setText("txtDIS " + version);
		shell.setImage(iconImage);
	}

	protected void show() {
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
		shell.open();
		while (!shell.isDisposed()) {
			if (!DIS.DISPLAY.readAndDispatch())
				DIS.DISPLAY.sleep();
		}
		iconImage.dispose();
	}

	protected Shell getShell() {
		return shell;
	}
}