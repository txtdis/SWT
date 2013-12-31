package ph.txtdis.windows;

import java.sql.Date;


public class Report {
	protected boolean[] hiddenItems;
	protected int id, partnerId, itemId;
	protected Data sql;
	protected Date date;
	protected Date[] dates;
	protected Integer categoryId, routeId;
	protected Object object;
	protected Object[] objects, totals;
	protected Object[][] data;
	protected String module, header, itemName;
	protected String[][] headers;
	
	public Report() {
		sql = new Data();
	}
	
	public Integer getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	public Object[][] getData() {
		return data;
	}

	public void setData(Object[][] data) {
		this.data = data;
	}
	
	public Date getDate() {
		if (date == null) 
			date = DIS.TODAY;
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Date[] getDates() {
		return dates;
	}

	public void setDates(Date[] dates) {
		this.dates = dates;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String[][] getHeaders() {
		return headers;
	}
	
	public boolean[] getHiddenItems() {
		return hiddenItems;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public int getPartnerId() {
		return partnerId;
	}

	public void setPartnerId(int partnerId) {
		this.partnerId = partnerId;
	}

	public Integer getRouteId() {
		return routeId;
	}

	public void setRouteId(Integer routeId) {
		this.routeId = routeId;
	}

	public Object[] getTotals() {
		return totals;
	}	
}
