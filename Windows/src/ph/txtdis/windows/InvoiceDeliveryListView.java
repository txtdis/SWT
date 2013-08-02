package ph.txtdis.windows;

import java.sql.Date;

public class InvoiceDeliveryListView extends ReportView {
	private Date[] dates;
	private int rowIdx;
	private Integer categoryId, colIdx;

	public InvoiceDeliveryListView(Date[] dates, int row, Integer col, Integer cat) {
		this.dates = dates;
		this.rowIdx = row;
		this.colIdx = col;
		this.categoryId = cat;
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
		if (categoryId != null) {
			report = new InvoiceDeliveryList(dates, rowIdx, colIdx, categoryId);
		} else if (colIdx == null) {
			report = new InvoiceDeliveryList(dates, rowIdx);
		} else {
			report = new InvoiceDeliveryList(dates, rowIdx, colIdx);
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

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		new InvoiceDeliveryListView(null, 3, 4, null);
		Database.getInstance().closeConnection();
	}
}
