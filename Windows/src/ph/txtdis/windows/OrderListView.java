package ph.txtdis.windows;

import java.sql.Date;

public class OrderListView extends ReportView implements Subheaderable {
	private Date[] dates;
	private int itemId;
	private Integer routeId, categoryId;
	private String orderType;

	public OrderListView(String orderType, Date[] dates, int itemId, Integer routeId, Integer categoryId) {
		this.orderType = orderType;
		this.dates = dates;
		this.itemId = itemId;
		this.routeId = routeId;
		this.categoryId = categoryId;
		runClass();
		addHeader();
		addSubheader();
		addTable();
		addTotalBar();
		show();
	}

	private void runClass() {
		Integer qcId = categoryId;
		switch (orderType) {
			case "count":
				Integer locationId = routeId;
				data = new CountList(dates[0], itemId, locationId, qcId);				
				break;
			case "outlet":
				data = new OutletList(dates, itemId, routeId);
				break;
			case "receiving":
				data = new ReceivingList(dates, itemId, routeId, qcId);				
				break;
			case "sales":
				data = new SalesList(dates, itemId, routeId, qcId);				
				break;
			case "sold":
			case "invoice":
				if (categoryId != null) {
					int outletId = itemId;
					data = new InvoiceDeliveryList(dates, outletId, routeId, categoryId);
				} else {
					data = new InvoiceDeliveryList(dates, itemId, routeId);
				}	
				break;
			default:
				break;
		}
	}

	@Override
	protected void addHeader() {
		new Header(this, data) {
			@Override
            protected void layButtons() {
	            // TODO Auto-generated method stub
	            
            }
		};
	}
 
	@Override
	public void addSubheader() {
		new Subheading(shell, (Subheaded) data);
	}

	@Override
    public Type getType() {
		return data.getType();
    }
}
