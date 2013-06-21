package ph.txtdis.windows;

import java.sql.Date;

public class InvoiceDiscrepancyView extends VatView {
	private InvoiceDiscrepancy invoiceDiscrepancy;

	public InvoiceDiscrepancyView(Date[] dates) {
		super(dates);
	}
	
	@Override
	protected void runClass() {
		report = invoiceDiscrepancy = new InvoiceDiscrepancy(dates);
	}
	
	@Override
	protected void setTitleBar() {
		new DateTitleBar(this, invoiceDiscrepancy);
	}
	
	@Override
	protected void setHeader() {
		new ReportHeaderBar(this.getShell(), invoiceDiscrepancy);
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		new InvoiceDiscrepancyView(null);
		Database.getInstance().closeConnection();
	}
}
