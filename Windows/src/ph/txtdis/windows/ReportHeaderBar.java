package ph.txtdis.windows;

import java.sql.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ReportHeaderBar {
	private int itemId, partnerId;
	private Customer customer;
	private Date date;
	private Date[] dates;
	private Integer categoryId, routeId;
	private ItemHelper item;
	private Route routing;
	private String start, end, itemName, module, partner, route, string;

	public ReportHeaderBar(Composite parent, Report report) {
		customer = new Customer();
		categoryId = report.getCategoryId();
		date = report.getDate();
		dates = report.getDates();
		item = new ItemHelper();
		itemId = report.getItemId();
		itemName = item.getName(itemId);
		module = report.getModule();
		partnerId = report.getPartnerId();
		partner = customer.getName(partnerId);
		routeId = report.getRouteId();
		if (routeId != null) {
			routing = new Route();
			route = routing.getName(routeId);
		}

		switch (module) {
		case "Bill of Materials":
			string = report.getItemName();
			break;
		case "Stock Take Tag List":
			string = itemName + "\ncounted";
			dates = new Date[] { date };
			break;
		case "Invoice/Delivery List":
			SoldList soldList = (SoldList) report;
			string = item.getFamily(soldList.getProductLineId()) + "\nsold/delivered to\n" + partner;
			if (categoryId == null)
				string = itemName + "\nsold/delivered ";
			if (routeId != null)
				string += "by\n" + route;
			break;
		case "Invoicing Discrepancies":
			string = module;
			break;
		case "Outlet List":
			OutletList outlet = (OutletList) report;
			string = item.getFamily(outlet.getProductLineId()) + " sold by " + route + "\n";
			break;
		case "Overdue Invoices":
			string = partner + " is on hold\nuntil the following are paid";
			break;
		case "Overdue Statement":
			string = partner;
			break;
		case "Receiving Report List":
			string = itemName + (routeId != null ? "\nback-loaded from " + route : "\nreturned/purchased");
			break;
		case "Load Settlement":
		case "Cash Settlement":
		case "Remittance Settlement":
			string = route;
			break;
		case "Sales Order List":
			string = itemName + "\nordered by " + route;
			break;
		case "Sales Report":
			SalesReport salesReport = (SalesReport) report;
			string = (salesReport.getMetric().equals("SALES TO TRADE") ? "Sales to Trade of " : "Productivity for ")
			        + item.getFamily(categoryId);
			break;
		case "Stock Take":
			string = "Summary of Count Conducted";
			dates = new Date[] { date };
			break;
		case "Stock Take Reconciliation":
			string = "Variance of System Inventory vs. Stock Take";
			break;
		case "Value-Added Tax":
			string = "VAT";
			break;
		default:
			System.out.println(module +"@reportheaderbar");
			break;
		}
		Label subtitle = new Label(parent, SWT.CENTER);
		int dateCount = dates == null ? 0 : dates.length;
		if (dateCount != 0) {
			start = DIS.LONG_DATE.format(dates[0]);
			if (dateCount > 1) {
				end = DIS.LONG_DATE.format(dates[1]);
				string += !start.equals(end) ? "\nfrom " + start + " to " + end : " on " + start;
			} else {
				string += " on " + start;
			}
		}
		report.setHeader(string);
		subtitle.setText(string);
		subtitle.setFont(UI.BOLD);
		subtitle.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
	}
}
