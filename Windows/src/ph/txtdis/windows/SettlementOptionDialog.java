package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class SettlementOptionDialog extends DialogView {
	private Combo routeCombo;
	private Report report;
	private Route route;

	public SettlementOptionDialog(Report report) {
		super();
		this.report = report;
		setName("Options");
		route = new Route();
		open();
	}

	@Override
	public void setRightPane() {
		Composite cmp = new Composite(header, SWT.NONE);
		cmp.setLayout(new GridLayout(1, false));
		routeCombo = new ComboBox(cmp, route.getList(), "ROUTE").getCombo();
	}

	@Override
	protected void setOkButtonAction() {
		int routeId = route.getId(routeCombo.getText());
		for (Shell sh : shell.getDisplay().getShells()) {
			sh.dispose();
		}
		String module = report.getModule();
		switch (module) {
		case "Load-In/Out Settlement":
			new SettlementView(new LoadSettlement(report.getDates(), routeId));
			break;
		case "Cash Settlement":
			new SettlementView(new CashSettlement(report.getDates(), routeId));
			break;
		case "Deposit/Transmittal Settlement":
			new SettlementView(new DepositSettlement(report.getDates(), routeId));
			break;
		default:
			new ErrorDialog("No Option for\n" + module + "\nin SettlementOptionDialog");
			break;
		}
	}
}
