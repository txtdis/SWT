package ph.txtdis.windows;

public class SalesTargetListView extends ReportView {

	public SalesTargetListView() {
		super();
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
		report = new SalesTargetList();
	}

	@Override
	protected void setTitleBar() {
		new ListTitleBar(this, report) {
			@Override
			protected void layButtons() {
				new AddButton(buttons, module);
				new ExcelButton(buttons, report);
			}
		};
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene", "ayin", "localhost");
		new SalesTargetListView();
		Database.getInstance().closeConnection();
	}

}
