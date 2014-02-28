package ph.txtdis.windows;

import java.sql.Date;
import java.util.Arrays;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

public class SettlementOptionDialog extends DialogView {
	private Combo typeCombo, routeCombo;
	private Data data;

	public SettlementOptionDialog(Data data) {
		super(Type.OPTION, "");
		this.data = data;
		display();
	}

	@Override
	public void setRightPane() {
		Composite cmp = new Compo(header, 2).getComposite();
		setTypeCombo(cmp);
		routeCombo = new ComboBox(cmp, Route.getList(), "ROUTE").getCombo();
	}

	private void setTypeCombo(Composite cmp) {
		String[] types = new String[] { "CASH", "LOAD", "REMITTANCE" };
		typeCombo = new ComboBox(cmp, types, "TYPE").getCombo();
		typeCombo.select(getIndex(types));
	}

	private int getIndex(String[] types) {
		String module = data.getType().getName();
		String[] settlementTypes = module.split(" ");
		String settlement = settlementTypes[0].toUpperCase();
		return Arrays.binarySearch(types, settlement);
	}

	@Override
	protected void setOkButtonAction() {
		int routeId = Route.getId(routeCombo.getText());
		String module = DIS.capitalize(typeCombo.getText());
		String className = DIS.PACKAGE + module + "Settlement";
		Object[] parameters = { data.getDates(), routeId };
		Class<?>[] parameterTypes = { Date[].class, int.class };
		Data settlement = DIS.instantiateClass(className, parameters, parameterTypes);
		
		shell.close();
		new SettlementView(settlement);
	}
}
