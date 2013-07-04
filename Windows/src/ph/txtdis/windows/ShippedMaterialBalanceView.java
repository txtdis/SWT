package ph.txtdis.windows;

import java.sql.Date;

public class ShippedMaterialBalanceView extends ReportView {
	private ShippedMaterialBalance route;
	private int routeId;
	private Date[] dates;
		
	public ShippedMaterialBalanceView(Date[] dates, int routeId) {
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
		report = route = new ShippedMaterialBalance(dates, routeId);
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
		new ShippedMaterialBalanceView(null, 1);
		Database.getInstance().closeConnection();
	}
}