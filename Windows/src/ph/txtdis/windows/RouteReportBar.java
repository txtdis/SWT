package ph.txtdis.windows;

public class RouteReportBar extends ListTitleBar {

	public RouteReportBar(RouteView view, RouteReport report) {
		super(view, report);
		new ExcelButton(buttons, report);
		new ExitButton(buttons, module);
	}
}
