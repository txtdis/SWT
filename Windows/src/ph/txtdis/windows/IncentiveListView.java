package ph.txtdis.windows;

public class IncentiveListView extends ReportView {
	
	public IncentiveListView() {
		super();
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
		report = new ProgramList();
	}

	@Override
	protected void setTitleBar() {
		new ListTitleBar(this, report){
			@Override
			protected void layButtons() {
				new AddButton(buttons, module);
				new ExcelButton(buttons, report);
				new ExitButton(buttons, module);			}
		};
	}
	
	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		new IncentiveListView();
		Database.getInstance().closeConnection();
	}


}
