package ph.txtdis.windows;

import java.sql.Date;

public class IrregularListView extends ReportView {

	private Date[] dates;
	private String string;

	public IrregularListView(Date[] dates) {
		this(dates, "");
	}

	public IrregularListView(String string) {
		this(null, string);
	}

	public IrregularListView(Date[] dates, String string) {
		super();
		this.dates = dates;
		this.string = string;
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
		report = new IrregularList(dates, string);
	}
}
