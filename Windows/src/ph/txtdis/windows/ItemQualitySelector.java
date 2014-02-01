package ph.txtdis.windows;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.TableItem;

public class ItemQualitySelector {

	public ItemQualitySelector(final OrderView view, final Expirable data) {
		final TableItem tableItem = view.getTableItem();
		final Combo qualityCombo = new TableCombo(tableItem, 4, new Quality().getStates()).getCombo();
		qualityCombo.setText(tableItem.getText(4));
		qualityCombo.setEnabled(true);
		qualityCombo.setFocus();
		new ComboSelector(qualityCombo, null) {
			@Override
			protected void processSelection() {
				Type quality = Type.valueOf(selection);
				data.setQuality(quality);
				qualityCombo.dispose();
				tableItem.setText(4, selection);
				new ItemExpiryInput(view, data, DIS.getDatePerQuality(quality));
			}
		};
	}
}
