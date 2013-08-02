package ph.txtdis.windows;

import java.sql.Date;

public class VatView extends ReportView {
	protected Date[] dates;
	protected Vat tax;
		
	public VatView(Date[] dates) {
		this.dates = dates;
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
		report = tax = new Vat(dates);
	}

	@Override
	protected void setTitleBar() {
		new DateTitleBar(this, tax);
	}
	
	@Override
	protected void setHeader() {
		new ReportHeaderBar(this.getShell(), tax);
	}
}