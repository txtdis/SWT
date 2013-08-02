package ph.txtdis.windows;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class DataEntry extends DataDisplay {
	private Color bground;

	public DataEntry(Composite cmp, String name, Object o) {
		super(cmp, name, o);
		switch (o.getClass().getSimpleName()) {
			case "BigDecimal":
				txt.setTextLimit(13);
				new DecimalVerifier(txt);
				break;
			case "Integer":
				txt.setTextLimit(7);
				new IntegerVerifier(txt);
				break;
			case "Long":
				txt.setTextLimit(13);
				new IntegerVerifier(txt);
				break;
			case "Date":
				txt.setText(StringUtils.rightPad(o.toString(), 6));
				new DateVerifier(txt);
				break;
			case "Time":
				new TimeVerifier(txt);
				break;
			case "String":
				txt.addVerifyListener(new VerifyListener() {
					@Override
					public void verifyText(VerifyEvent e) {
						e.text = e.text.toUpperCase();
					}
				});
				break;
			default:
				new ErrorDialog(
						"No DataEntry option for\n" +
						o.getClass().getSimpleName());
				break;
		}
	}

	public DataEntry(Composite cmp, String name, String s, int span, int size) {
		this(cmp, name, s, span);
		txt.setText(StringUtils.rightPad(s == null ? "" : s, size));
		txt.setTextLimit(size);
	}

	public DataEntry(Composite cmp, String name, String s, int span) {
		super(cmp, name, s, span);
		txt.addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				e.text = e.text.toUpperCase();
			}
		});
	}

	public DataEntry(Composite cmp, Object o) {
		this(cmp, null, o);
	}

	@Override
	public Text getText() {
		super.getText();
		txt.setToolTipText("Double Click to Edit");

		txt.addListener(SWT.MouseDoubleClick, new Listener(){
			@Override
			public void handleEvent(Event event) {
				txt.setTouchEnabled(true);
				txt.setFocus();
				txt.setEditable(true);
				txt.setBackground(DIS.YELLOW);
				txt.selectAll();
			}		
		});

		txt.addFocusListener(new FocusListener(){
			@Override
			public void focusGained(FocusEvent e) {
				if (txt.getTouchEnabled()) {
					txt.setEditable(true);
					bground = txt.getBackground();
					txt.setText(txt.getText().trim());
					txt.setBackground(DIS.YELLOW);
					txt.selectAll();
				} else {
					txt.setEditable(false);
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				txt.setBackground(bground);
				txt.setTouchEnabled(false);
			}
		});
		return txt;
	}
}
