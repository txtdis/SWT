package ph.txtdis.windows;

import java.sql.Date;

import org.eclipse.swt.widgets.Composite;

public class ReportGenerationButton extends ReportButton {

	public ReportGenerationButton(Composite parent, Data report) {
		super(parent, report, "Report", "Generate Report");
		module = report.getType().getName();
		
	}
	
	@Override
    protected void proceed() {
		switch (module) {
			case "Inventory":
				new InventoryReportGeneration((Inventory) data); 
				break;
			case "Stock Take ":
				Date date = ((CountData) data).getDate();
				new InventoryReportGeneration(new CountData(date)); 
				break;
			case "Sales Report":
				new SalesReportGeneration((SalesReport) data); 
				break;
			case "Purchase Order":
				new PurchaseOrderGeneration(((InputData) data).getId()); 
				break;
			default:
				System.out.println(module + "@ReportGenerationButton");
		}
	}
}