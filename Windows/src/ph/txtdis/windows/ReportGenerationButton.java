package ph.txtdis.windows;

import java.sql.Date;

import org.eclipse.swt.widgets.Composite;

public class ReportGenerationButton extends ReportButton {

	public ReportGenerationButton(Composite parent, Report report) {
		super(parent, report, "Report", "Generate Report");
		module = report.getModule();
	}

	@Override
	protected void doWithProgressMonitorWhenSelected() {
		switch (module) {
			case "Inventory":
				new InventoryReportGeneration((Inventory) report); 
				break;
			case "Stock Take":
				Date date = ((StockTake) report).getPostDate();
				new InventoryReportGeneration(new StockTake(date)); 
				break;
			case "Sales Report":
				new SalesReportGeneration((SalesReport) report); 
				break;
			case "Purchase Order":
				new PurchaseOrderGeneration(report.getId()); 
				break;
			default:
				new InfoDialog("Nothing to report");
		}
	}
}