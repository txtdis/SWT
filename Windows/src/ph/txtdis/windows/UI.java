package ph.txtdis.windows;

import java.io.InputStream;
import java.math.BigDecimal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

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
	public final static Font MONO = new Font(UI.DISPLAY, "Consolas", 10, SWT.NORMAL);
	public final static Font REG = new Font(UI.DISPLAY, "Ubuntu", 10, SWT.NORMAL);
	public final static Font BIG = new Font(UI.DISPLAY, "Ubuntu", 24, SWT.BOLD | SWT.ITALIC);
	public final static Font BOLD = new Font(UI.DISPLAY, "Ubuntu", 18, SWT.BOLD);
	
	public static Image createImage(String directory, Type type, String suffix) {
		InputStream image = new UI().getClass().getResourceAsStream(directory + "/" + type + suffix + ".png"); 
		return new Image(UI.DISPLAY, image);
	}

	public static int extractInt(TableItem tableItem, int columnIdx) {
		String number = tableItem.getText(columnIdx);
		return DIS.parseInt(number);
	}

	public static String extractString(TableItem tableItem, int columnIdx) {
		return tableItem.getText(columnIdx);
	}
	
	public static void setTableItemText(TableItem tableItem, int columnIdx, int number) {
		tableItem.setText(columnIdx, DIS.INTEGER.format(number));
		tableItem.setForeground(columnIdx, setColor(number));
	}

	public static void setTableItemText(TableItem tableItem, int columnIdx, BigDecimal number) {
		tableItem.setText(columnIdx, DIS.formatTo2Places(number));
		tableItem.setForeground(columnIdx, setColor(number));
	}

	public static void setTableItemText(Text text, int columnIdx, int number) {
		text.setText(DIS.INTEGER.format(number));
		text.setForeground(setColor(number));
	}

	public static void setText(Text text, BigDecimal number) {
		text.setText(DIS.formatTo2Places(number));
		text.setForeground(setColor(number));
	}
	
	public static Color setColor(int number) {
		return number < 0 ? UI.RED : UI.BLACK;
	}
	
	public static Color setBackColor(int rowIdx) {
		return rowIdx % 2 == 0 ? UI.WHITE : UI.GRAY;
	}
	
	public static Color setColor(BigDecimal number) {
		return DIS.isNegative(number) ? UI.RED : UI.BLACK;
	}
	
	public static boolean isNull(Control control) {
		return control == null || control.isDisposed();
	}
	
	public static void closeApp() {
		for (Shell sh : UI.DISPLAY.getShells())
			sh.dispose();	            
	}
	
	public static void goToControl(Control next) {
		if (UI.isNull(next))
			return;
		prepareControlForInput(next);
		next.setFocus();
	}

	public static Control prepareControlForInput(Control next) {
		if (next instanceof Text)
			next = addTextSpecificInputProperties((Text) next);
		next.setEnabled(true);
		return next;
	}

	private static Text addTextSpecificInputProperties(Text text) {
		text.setEditable(true);
		text.selectAll();
        text.setBackground(YELLOW);
		return text;
	}

}

