package ph.txtdis.windows;

public class ReceivingTitleBar extends ListTitleBar{
	
	public ReceivingTitleBar(ReportView view, Report report) {
		super(view, report);
	}
	
	@Override 
	protected void layButtons() {
		new NewButton(buttons, module);
		new RetrieveButton(buttons, report);	
		((ReceivingView) view).setBtnPost(new PostButton(buttons, reportView, report).getButton());
		new ExitButton(buttons, module);
	}
}
