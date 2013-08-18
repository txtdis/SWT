package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class TextDisplayBox {
	private Label label;
	protected Text text;
	protected Composite composite;

	public TextDisplayBox(Composite parent, String name, Object initialDatum) {
		this(parent, name);
		switch (initialDatum.getClass().getSimpleName()) {
			case "BigDecimal":
				BigDecimal decimal = (BigDecimal) initialDatum;
				text = new Text(parent, SWT.BORDER | SWT.RIGHT);
				text.setText(StringUtils.leftPad(
				        decimal.compareTo(BigDecimal.ZERO) == 0 ? "" : DIS.TWO_PLACE_DECIMAL.format(decimal), 13));
				text.setTextLimit(13);
				setText();
				break;
			case "Integer":
				int integer = (int) initialDatum;
				text = new Text(parent, SWT.BORDER | SWT.RIGHT);
				text.setText(StringUtils.leftPad(integer == 0 ? "" : "" + integer, 7));
				setText();
				break;
			case "Long":
				long longInteger = (long) initialDatum;
				text = new Text(parent, SWT.BORDER | SWT.RIGHT);
				text.setText(StringUtils.leftPad(longInteger == 0 ? "" : "" + longInteger, 13));
				setText();
				break;
			case "Date":
				Date date = (Date) initialDatum;
				text = new Text(parent, SWT.BORDER | SWT.LEFT);
				text.setText(DIS.POSTGRES_DATE.format(date));
				text.setTextLimit(10);
				setText();
				break;
			case "Time":
				Time time = (Time) initialDatum;
				text = new Text(parent, SWT.BORDER | SWT.LEFT);
				text.setText(DIS.TIME.format(time));
				text.setTextLimit(5);
				setText();
				break;
			case "String":
				String string = (String) initialDatum;
				text = new Text(parent, SWT.BORDER | SWT.LEFT);
				text.setText(string);
				setText();
				break;
			default:
				new ErrorDialog("No DataDisplay option for\n" + initialDatum.getClass().getSimpleName());
				setText();
				break;
		}
	}

	public TextDisplayBox(Composite parent, String name, String defaultText, int horizontalSpan) {
		this(parent, name);
		text = new Text(parent, SWT.BORDER | SWT.LEFT);
		text.setText(defaultText == null ? "" : defaultText);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, horizontalSpan, 1));
		setText();
	}

	public TextDisplayBox(Composite parent, String name) {
		this.composite = parent;
		if (name != null) {
			label = new Label(parent, SWT.RIGHT);
			label.setText(name);
			label.setFont(DIS.MONO);
			label.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, true, 1, 1));
		}
	}

	private void setText() {
		text.setBackground(DIS.WHITE);
		text.setFont(DIS.MONO);
		text.setEditable(false);
		text.addListener(SWT.Modify, new Listener() {
			@Override
			public void handleEvent(Event event) {
				String textInput = text.getText();
				if (textInput.contains("(") && textInput.contains(")")) {
					text.setForeground(DIS.RED);
				}
			}
		});
	}

	public Text getText() {
		return text;
	}

	public Label getLabel() {
		return label;
	}
}
