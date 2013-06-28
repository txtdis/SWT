package ph.txtdis.windows;

import java.sql.Date;

public class ShippingMaterialBalanceView extends ReportView {
	private ShippingMaterialBalance route;
	private int routeId;
	private Date[] dates;
		
	public ShippingMaterialBalanceView(Date[] dates, int routeId) {
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
		report = route = new ShippingMaterialBalance(dates, routeId);
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
		new ShippingMaterialBalanceView(null, 1);
		Database.getInstance().closeConnection();
	}
}