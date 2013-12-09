package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

public class UI {
	// COLOR
	public final static Display DISPLAY = new Display();
	public final static Color WHITE = DISPLAY.getSystemColor(SWT.COLOR_WHITE);
	public final static Color YELLOW = DISPLAY.getSystemColor(SWT.COLOR_YELLOW);
	public final static Color GRAY = DISPLAY.getSystemColor(SWT.COLOR_GRAY);
	public final static Color BLUE = DISPLAY.getSystemColor(SWT.COLOR_BLUE);
	public final static Color GREEN = DISPLAY.getSystemColor(SWT.COLOR_DARK_GREEN);
	public final static Color RED = DISPLAY.getSystemColor(SWT.COLOR_RED);
	public final static Color BLACK = DISPLAY.getSystemColor(SWT.COLOR_BLACK);

	// FONT
	public final static Font MONO = new Font(UI.DISPLAY, "Ubuntu Mono", 10, SWT.NORMAL);
	public final static Font REG = new Font(UI.DISPLAY, "Ubuntu", 10, SWT.NORMAL);
	public final static Font BIG = new Font(UI.DISPLAY, "Ubuntu", 24, SWT.BOLD | SWT.ITALIC);
	public final static Font BOLD = new Font(UI.DISPLAY, "Ubuntu", 18, SWT.BOLD);

}

