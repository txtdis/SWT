package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class DataDisplay {
	private Label lbl;
	protected Text txt;
	protected Composite cmp;

	public DataDisplay(Composite cmp, String name, Object o) {
		this(cmp, name);
		switch (o.getClass().getSimpleName()) {
			case "BigDecimal":
				BigDecimal bd = (BigDecimal) o;
				txt = new Text (cmp, SWT.BORDER | SWT.RIGHT);
				txt.setText(StringUtils.leftPad(
						bd.equals(BigDecimal.ZERO) ? "" : DIS.TWO_PLACE_DECIMAL.format(bd), 13));
				txt.setTextLimit(13);
				setText();
				break;
			case "Integer":
				int i = (int) o;
				txt = new Text (cmp, SWT.BORDER | SWT.RIGHT);
				txt.setText(StringUtils.leftPad(i == 0 ? "" : "" + i, 7));
				setText();
				break;
			case "Long":
				long l = (long) o;
				txt = new Text (cmp, SWT.BORDER | SWT.RIGHT);
				txt.setText(StringUtils.leftPad(l == 0 ? "" : "" + l, 13));
				setText();
				break;
			case "Date":
				Date date = (Date) o;
				txt = new Text (cmp, SWT.BORDER | SWT.LEFT);
				txt.setText(new SimpleDateFormat("yyyy-MM-dd").format(date));
				txt.setTextLimit(10);
				setText();
				break;
			case "Time":
				Time time = (Time) o;
				txt = new Text (cmp, SWT.BORDER | SWT.LEFT);
				txt.setText(new SimpleDateFormat("HH:mm").format(time));
				txt.setTextLimit(5);
				setText();
				break;
			case "String":
				String string = (String) o;
				txt = new Text (cmp, SWT.BORDER | SWT.LEFT);
				txt.setText(string);
				setText();
				break;
			default:
				new ErrorDialog("" +
						"No DataDisplay option for\n" +
						o.getClass().getSimpleName());
				setText();
				break;
		}
	}

	public DataDisplay(Composite cmp, String label, String s, int horizontalSpan) {
		this(cmp, label);
		txt = new Text (cmp, SWT.BORDER | SWT.LEFT);
		txt.setText(s == null ? "" : s);
		txt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, horizontalSpan, 1));
		setText();
	}

	public DataDisplay(Composite cmp, String name) {
		this.cmp = cmp;
		if(name != null) {
			lbl = new Label (cmp, SWT.RIGHT);
			lbl.setText(name);
			lbl.setFont(DIS.REG);
			lbl.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, true, 1, 1));
		}
	}

	private void setText() {
		txt.setFont(DIS.MONO);
		txt.setTouchEnabled(false);
		txt.setEditable(false);
		txt.setBackground(DIS.WHITE);
		txt.addListener(SWT.Modify, new Listener() {
			@Override
            public void handleEvent(Event event) {
				String text = txt.getText();
				if (text.contains("(") && text.contains(")")) {
					txt.setForeground(DIS.RED);
				}
			}
        });
	}
	public Text getText() {
		return txt;
	}
	
	public Label getLabel() {
		return lbl;
	}
}
