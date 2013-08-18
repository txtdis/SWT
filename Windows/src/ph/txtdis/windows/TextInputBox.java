package ph.txtdis.windows;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class TextInputBox extends TextDisplayBox {

	public TextInputBox(Composite parent, Object initialDatum) {
		this(parent, null, initialDatum);
	}

	public TextInputBox(Composite parent, String name, Object initialDatum) {
		super(parent, name, initialDatum);
		switch (initialDatum.getClass().getSimpleName()) {
			case "BigDecimal":
				text.setTextLimit(13);
				new DecimalVerifier(text);
				break;
			case "Integer":
				text.setTextLimit(7);
				new IntegerVerifier(text);
				break;
			case "Long":
				text.setTextLimit(13);
				new IntegerVerifier(text);
				break;
			case "Date":
				text.setText(StringUtils.rightPad(initialDatum.toString(), 6));
				new DateVerifier(text);
				break;
			case "Time":
				new TimeVerifier(text);
				break;
			case "String":
				text.addVerifyListener(new VerifyListener() {
					@Override
					public void verifyText(VerifyEvent event) {
						event.text = event.text.toUpperCase();
					}
				});
				break;
			default:
				new ErrorDialog("No DataEntry option for\n" + initialDatum.getClass().getSimpleName());
				break;
		}
	}

	public TextInputBox(Composite parent, String name, String initialText, int horizontalSpan, int textLimit) {
		this(parent, name, initialText, horizontalSpan);
		text.setText(StringUtils.rightPad(initialText == null ? "" : initialText, textLimit));
		text.setTextLimit(textLimit);
	}

	public TextInputBox(Composite parent, String name, String initialText, int horizontalSpan) {
		super(parent, name, initialText, horizontalSpan);
		text.addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				e.text = e.text.toUpperCase();
			}
		});
	}

	@Override
	public Text getText() {
		super.getText();
		text.setToolTipText("Double Click to Edit");
		
		text.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				text.selectAll();
				text.setFocus();
			}
		});

		text.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				text.setEditable(true);
				text.setBackground(DIS.YELLOW);
				text.selectAll();
			}
			
			@Override
			public void focusLost(FocusEvent e) {
				text.setEditable(false);
				text.setBackground(DIS.WHITE);
			}
		});
		return text;
	}
}
