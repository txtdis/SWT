package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;

import org.eclipse.swt.widgets.TableItem;

public class StockTakeSaving {
	private StockTakeView view;
	private StockTake st;

	public StockTakeSaving(StockTakeView view, StockTake st) {
		this.view = view;
		this.st = st;
	}

	public StockTake get() {
		try {
			// Header Data
			st.setTakerId(new Employee(view.getCmbTaker().getText()).getId());
			st.setCheckerId(new Employee(view.getCmbChecker().getText()).getId());
			st.setLocationId(new Location(view.getCmbLocation().getText()).getId());
			st.setPostDate(new Date(DIS.POSTGRES_DATE.parse(view.getTxtDate().getText()).getTime()));
			//
			TableItem[] tableItems = view.getTable().getItems(); 
			ArrayList<ItemCount> itemCount = new ArrayList<>();
			for (int i = 0; i < tableItems.length; i++) {
				//[1]item_id, [3]uom, [4]qty, [5]qc_id, [6] expiry
				int id = Integer.parseInt(tableItems[i].getText(1));	
				int uom = new UOM(tableItems[i].getText(3)).getId();	
				BigDecimal qty = new BigDecimal(tableItems[i].getText(4));
				int qc = new Quality(tableItems[i].getText(5)).getId();	
				Date date = new Date(DIS.POSTGRES_DATE.parse(tableItems[i].getText(6)).getTime());
				itemCount.add(new ItemCount(id, uom, qc, qty, date));
			}
			st.setItemCount(itemCount);
			return st;
		} catch (Exception e) {
			e.printStackTrace();
			new ErrorDialog(e);
			return null;
		}
	}
}
