package ph.txtdis.windows;


public class Report {
	protected String[][] headers;
	protected boolean[] hiddenItems;
	protected Object[][] data;
	protected Object[] totals;
	protected String module, header;
	protected int id;
			
	public Report() {
	}

	public String[][] getHeaders() {
		return headers;
	}
	
	public boolean[] getHiddenItems() {
		return hiddenItems;
	}

	public Object[][] getData() {
		return data;
	}

	public void setData(Object[][] data) {
		this.data = data;
	}
	
	public Object[] getTotals() {
		return totals;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
