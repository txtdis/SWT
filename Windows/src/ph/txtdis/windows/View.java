package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

public abstract class View {

	protected Shell shell;
	protected Type type;
	private Image icon;

	public View() {
		createShell();
		setIcon();
		shell.setLayout(new GridLayout(1, false));
	}

	protected abstract void proceed();

	protected void show() {
		center();
		shell.open();
		loopUntilDisposed();
	}

	private void loopUntilDisposed() {
		while (!shell.isDisposed())
			if (!UI.DISPLAY.readAndDispatch())
				UI.DISPLAY.sleep();
		if (icon != null)
			icon.dispose();
	}

	protected void center() {
		shell.pack();
		setLocation(getMonitorBounds(), shell.getBounds());
	}

	private Rectangle getMonitorBounds() {
		Monitor primary = shell.getMonitor();
		return primary.getBounds();
	}

	private void setLocation(Rectangle monitorBounderies, Rectangle shellBounderies) {
		int y = getY(monitorBounderies, shellBounderies);
		int x = getX(monitorBounderies, shellBounderies);
		shell.setLocation(x, y);
	}

	private int getX(Rectangle monitorBounderies, Rectangle shellBounderies) {
		int x = monitorBounderies.x + (monitorBounderies.width - shellBounderies.width) / 2;
		return monitorBounderies.width < shellBounderies.width ? 0 : x;
	}

	private int getY(Rectangle monitorBounderies, Rectangle shellBounderies) {
		int y = monitorBounderies.y + (monitorBounderies.height - shellBounderies.height) / 2;
		return monitorBounderies.height - 70 < shellBounderies.height ? 0 : y;
	}

	private void setIcon() {
		icon = UI.createImage("buttons", Type.ICON, "");
		shell.setImage(icon);
	}

	protected void createShell() {
		shell = new Shell(UI.DISPLAY, SWT.CLOSE);
		shell.setText("txtDIS " + Login.version() + "@" + Login.server() + "." + Login.network());
	}

	public Type getType() {
		return type;
	}

	public Shell getShell() {
		return shell;
	}
}