package ph.txtdis.windows;

public class DateTitleBar extends ListTitleBar {
	
	public DateTitleBar(ReportView view, Report report) {
		super(view, report);	
	}

	@Override
	protected void layButtons() {
		new CalendarButton(buttons, report);
		new BackwardButton(buttons, report);
		new ForwardButton(buttons, report);
		new ExcelButton(buttons, report);
		new ExitButton(buttons, module);
	}
}
