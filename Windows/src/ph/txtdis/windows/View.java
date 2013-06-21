package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

public abstract class View {

	protected static Display display;
	private static Font bigFont, boldFont, monoFont;
	private static Color white, yellow, gray, blue, green, red, black;
	protected Shell shell;
	private Image iconImage;

	public View () {
		if(display == null) display = new Display();
		setShell();
		white = display.getSystemColor(SWT.COLOR_WHITE);
		yellow = display.getSystemColor(SWT.COLOR_YELLOW);
		gray = display.getSystemColor(SWT.COLOR_GRAY);
		blue = display.getSystemColor(SWT.COLOR_BLUE);
		green = display.getSystemColor(SWT.COLOR_DARK_GREEN);
		red = display.getSystemColor(SWT.COLOR_RED);
		black = display.getSystemColor(SWT.COLOR_BLACK);

		bigFont = new Font(display, "Calibri", 24, SWT.BOLD | SWT.ITALIC);
		boldFont = new Font(display, "Calibri", 18, SWT.BOLD);
		monoFont = new Font(display, "Consolas", 10, SWT.NORMAL);

		iconImage = new Image(
				display, this.getClass().getResourceAsStream("images/icon.png"));
		String version = "0.9.7.4";

		shell.setLayout(new GridLayout(1, true));
		shell.setText("txtDIS " + version);
		shell.setImage(iconImage);
	}

	protected void show() {
		shell.pack();
		Monitor primary =  shell.getMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = shell.getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height  - rect.height) / 2;
		if (bounds.height - 70 < rect.height) y = 0;
		if (bounds.width  < rect.width) x = 0;
		shell.setLocation(x, y);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		bigFont.dispose();
		boldFont.dispose();
		monoFont.dispose();
		iconImage.dispose();
	}

	public Shell getShell() {
		return shell;
	}

	protected void setShell() {
		shell = new Shell(display);
	}

	public static Font bigFont() {
		return bigFont;
	}

	public static Font boldFont() {
		return boldFont;
	}

	public static Font monoFont() {
		return monoFont;
	}

	public static Display display() {
		return display;
	}

	public static Color white() {
		return white;
	}	

	public static Color gray() {
		return gray;
	}

	public static Color blue() {
		return blue;
	}

	public static Color green() {
		return green;
	}

	public static Color red() {
		return red;
	}

	public static Color black() {
		return black;
	}

	public static Color yellow() {
		return yellow;
	}
}