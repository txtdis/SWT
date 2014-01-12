package ph.txtdis.windows;

import java.sql.Date;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

public class SettlementOptionDialog extends DialogView {
	private Combo typeCombo, routeCombo;
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
		Composite cmp = new Compo(header, 2).getComposite();
		setTypeCombo(cmp);
		routeCombo = new ComboBox(cmp, route.getList(), "ROUTE").getCombo();
	}

	private void setTypeCombo(Composite cmp) {
		String[] types = new String[] { "CASH", "LOAD", "REMITTANCE" };
		typeCombo = new ComboBox(cmp, types, "TYPE").getCombo();
		typeCombo.select(getIndex(types));
	}

	private int getIndex(String[] types) {
		String module = report.getModule();
		String[] settlementTypes = module.split(" ");
		String settlement = settlementTypes[0].toUpperCase();
		return Arrays.binarySearch(types, settlement);
	}

	@Override
	protected void setOkButtonAction() {
		int routeId = route.getId(routeCombo.getText());
		String module = StringUtils.capitalize(typeCombo.getText().toLowerCase());
		String className = "ph.txtdis.windows." + module + "Settlement";
		Object[] parameters = { report.getDates(), routeId };
		Class<?>[] parameterTypes = { Date[].class, int.class };
		Report settlement = DIS.createObject(className, parameters, parameterTypes);
		UI.disposeAllShells(shell);
		new SettlementView(settlement);
	}
}
