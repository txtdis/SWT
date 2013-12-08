package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class UI {
	private static UI ui;
	private Display display;
	private Font monoFont, regFont, boldFont, bigFont;
	
	private UI() {
		
	}

	public static UI getInstance() {
		if (ui == null) {
			ui = new UI();
		}
		return ui;
	}
	
	public Display getDisplay() {
		if (display == null) {
			display = new Display();
			display.addListener(SWT.Dispose, new Listener() {
				@Override
				public void handleEvent(Event event) {
					if (monoFont != null || !monoFont.isDisposed())
						monoFont.dispose();
					if (regFont != null || !regFont.isDisposed())
						regFont.dispose();
					if (boldFont != null || !boldFont.isDisposed())
						boldFont.dispose();
					if (bigFont != null || !bigFont.isDisposed())
						bigFont.dispose();
				}
			});
		}
		return display;
	}
	
	public Font getMonoFont() {
		if (monoFont == null) {
			monoFont = new Font(getDisplay(), "Ubuntu Mono", 10, SWT.NORMAL);
		}
		return monoFont;
	}

	public Font getRegFont() {
		if (regFont == null) {
			regFont = new Font(getDisplay(), "Ubuntu", 10, SWT.NORMAL);
		}
		return regFont;
	}

	public Font getBoldFont() {
		if (boldFont == null) {
			boldFont = new Font(getDisplay(), "Ubuntu", 18, SWT.BOLD);
		}
		return boldFont;
	}

	public Font getBigFont() {
		if (bigFont == null) {
			bigFont = new Font(getDisplay(), "Ubuntu", 24, SWT.BOLD | SWT.ITALIC);
		}
		return bigFont;
	}
}
