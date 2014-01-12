package ph.txtdis.windows;

public class FilterTitleBar extends ListTitleBar {
	
	public FilterTitleBar(ReportView view, Report report) {
		super(view, report);	
	}

	@Override
	protected void layButtons() {
		new OptionButton(buttons, report);
		new CalendarButton(buttons, report);
		new BackwardButton(buttons, report);
		new ForwardButton(buttons, report);
		new ExcelButton(buttons, report);
	}
}
