package ph.txtdis.windows;

import java.sql.Date;
import java.sql.SQLException;

import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class CountVariancePosting extends Posting {

	public CountVariancePosting(CountVarianceView view) {
		super(view);
		save();
	}

	@Override
	protected void postData() throws SQLException {
		int itemId, qcId, adjustment;
		String reason, stmt;
		CountVarianceView view = (CountVarianceView) this.view;
		Date countDate = view.getEndDate();
		Table table = view.getTable();
		stmt = "INSERT INTO count_adjustment (count_date, item_id, qc_id, qty, reason)\n"
				+ "    VALUES(?, ?, ?, ?, ?);\n";

		for (TableItem tableItem : table.getItems()) {
			String quality = UI.extractString(tableItem, view.QC_COLUMN);
			qcId =  new Quality(Type.valueOf(quality)).getId();
			itemId = UI.extractInt(tableItem, view.ITEM_ID_COLUMN);
			adjustment = UI.extractInt(tableItem, view.ADJUSTMENT_COLUMN);
			reason = UI.extractString(tableItem, view.REASON_COLUMN);
			
			if(reason.isEmpty())
				continue;
			ps = conn.prepareStatement(stmt);
			ps.setDate(1, countDate);
			ps.setInt(2, itemId);
			ps.setInt(3, qcId);
			ps.setInt(4, adjustment);
			ps.setString(5, reason);
			ps.executeUpdate();
		}
		
		stmt = "INSERT INTO count_closure (count_date) VALUES(?);\n";
		ps = conn.prepareStatement(stmt);
		ps.setDate(1, countDate);
		ps.executeUpdate();
	}
}
