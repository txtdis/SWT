package ph.txtdis.windows;

import java.sql.Date;

public class OrderListView extends ReportView {
	private Date[] dates;
	private int itemId;
	private Integer routeId, categoryId;
	private String orderType;

	public OrderListView(String orderType, Date[] dates, int itemId) {
		this(orderType, dates, itemId, null, null);
	}
	
	public OrderListView(String orderType, Date[] dates, int itemId, Integer routeId) {
		this(orderType, dates, itemId, routeId, null);
	}
	
	public OrderListView(String orderType, Date[] dates, int itemId, Integer routeId, Integer categoryId) {
		this.orderType = orderType;
		this.dates = dates;
		this.itemId = itemId;
		this.routeId = routeId;
		this.categoryId = categoryId;
		setProgress();
		setTitleBar();
		setHeader();
		getTable();
		setTotalBar();
		setFooter();
		setListener();
		setFocus();
		showReport();
	}

	@Override
	protected void runClass() {
		switch (orderType) {
			case "count":
				Integer locationId = routeId;
				report = new StockTakeList(dates[0], itemId, locationId);				
				break;
			case "outlet":
				report = new OutletList(dates, itemId, routeId, categoryId);
				break;
			case "receiving":
				report = new ReceivingList(dates, itemId, routeId);				
				break;
			case "sales":
				report = new SalesOrderList(dates, itemId, routeId);				
				break;
			case "sold":
			case "invoice":
				System.out.println("cat: " + categoryId);
				System.out.println("route: " + routeId);
				if (categoryId != null) {
					int outletId = itemId;
					report = new SoldList(dates, outletId, routeId, categoryId);
				} else if (routeId == null) {
					report = new SoldList(dates, itemId);
				} else {
					report = new SoldList(dates, itemId, routeId);
				}	
				break;
			default:
				break;
		}
	}

	@Override
	protected void setTitleBar() {
		new ReportTitleBar(this, report);
	}
 
	@Override
	protected void setHeader() {
		new ReportHeaderBar(shell, report);
	}
}
