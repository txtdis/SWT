package ph.txtdis.windows;

import java.sql.Date;

public class FinanceView extends ReportView implements Subheaderable {

	public FinanceView() {
		this(null);
    }

	public FinanceView(Date[] dates) {
		super(new Vat(dates));
		type = Type.FINANCE;
		addHeader();
		addSubheader();
		addTable();
		addTotalBar();
		show();
	}
	
	@Override
	protected void addHeader() {
		new Header(this, data) {
			@Override
            protected void layButtons() {
				new BackwardButton(buttons, data);
				new CalendarButton(buttons, data);
				new ForwardButton(buttons, data);
				new ImgButton(buttons, Type.EXCEL, view);
            }
		};
	}
	
	@Override
    public void addSubheader() {
		new Subheading(shell, (Subheaded) data);
	}
}