package ph.txtdis.windows;

import java.sql.Date;

public class PurchaseTargetView extends OrderView {
	private Date date;
	
	public PurchaseTargetView(Date date) {
		super();
		this.date = date;
		setProgress();
		setTitleBar();
		setHeader();
		getTable();
		setFooter();
		setListener();
		setFocus();
		showReport();
	}

	@Override
	protected void runClass() {
		report = order = new PurchaseTarget(date);
	}

	@Override
	protected void setTitleBar() {
		new ListTitleBar(this, report){
			@Override
			protected void layButtons() {
				new BackwardButton(buttons, report);
				new ForwardButton(buttons, report);
				new PostButton(buttons, order);
				new ExcelButton(buttons, report);
			}
		};
	}
	
	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin","localhost");
		new PurchaseTargetView(null);
		Database.getInstance().closeConnection();
	}


}
