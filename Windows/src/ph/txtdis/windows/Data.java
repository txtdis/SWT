package ph.txtdis.windows;

import java.sql.Date;

public abstract class Data {
	protected Date date;
	protected Date[] dates;
	protected Object[][] tableData;
	protected String[][] tableHeaders;
	protected Query sql;
	protected Type type;
	
	public Data() {
		sql = new Query();
	}
	
	public Object[][] getTableData() {
		return tableData;
	}

	public void setData(Object[][] data) {
		this.tableData = data;
	}
	
	public Date getDate() {
		return date == null ? DIS.TODAY : date;
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

	public String[][] getTableHeaders() {
		return tableHeaders;
	}
	
	public Type getType() {
	    return type;
    }
}
