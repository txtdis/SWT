package ph.txtdis.windows;

import java.sql.Date;

public class RouteView extends ReportView {
	private RouteReport route;
	private int routeId;
	private Date[] dates;
		
	public RouteView(Date[] dates, int routeId) {
		this.routeId = routeId;
		this.dates = dates;
		setProgress();
		setTitleBar();
		setHeader();
		setTableBar();
		setFooter();
		setListener();
		setFocus();
		showReport();
	}
	
	@Override
	protected void runClass() {
		report = route = new RouteReport(dates, routeId);
	}
	
	@Override
	protected void setTitleBar() {
		new FilterTitleBar(this, route);
	}
	
	@Override
	protected void setHeader() {
		new ReportHeaderBar(shell, route);
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		new RouteView(null, 1);
		Database.getInstance().closeConnection();
	}
}