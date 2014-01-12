package ph.txtdis.windows;

import java.sql.Date;

import org.eclipse.swt.widgets.Composite;

public class CompletionButton extends ReportButton {
	private StockTake stockTake;
	private Date date; 

	public CompletionButton(Composite parent, StockTake stockTake) {
		super(parent, stockTake, "Stop", "Confirm Task Closure");
		this.stockTake = stockTake;
	}

	@Override
	protected void doWhenSelected() {
		new WarningDialog("Tagging this date as closed\nmeans no more count input.\n\nAre you sure?") {

			@Override
			protected void setOkButtonAction() {
				stockTake.closeDataEntry();
				if (stockTake.isDataEntryClosed(date)) {
					UI.disposeAllShells(parent);
					new StockTakeView(date);
				}
			}

		};
	}

}
