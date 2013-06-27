package ph.txtdis.windows;

import java.sql.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ReportHeaderBar {
	private String start, end;
	private Date[] dates;

	public ReportHeaderBar(Composite parent, Report report) {
		String string = "";
		switch (report.getModule()) {
			case "Bill of Materials":
				dates = new Date[0];
				string = new ItemHelper().getName(((BomList) report)
						.getItemId());
				string = string == null ? "" : string;
				break;
			case "Stock Take Tag List":
				StockTakeList cl = (StockTakeList) report;
				string += new ItemHelper().getName(cl.getItemId());
				string += "\ncounted";
				dates = new Date[] { cl.getDate() };
				break;
			case "Invoice/Delivery List":
				InvoiceDeliveryList il = (InvoiceDeliveryList) report;
				if (il.getCategoryId() == null) {
					string += new ItemHelper().getName(il.getItemId());
					string += "\nsold/delivered ";
					if (il.getRouteId() != null)
						string += "by "
								+ new Route(il.getRouteId()).getName();
					string += "\n";

				} else {
					string += new ItemHelper().getFamilyName(il
							.getProductLineId());
					string += " sold/delivered to ";
					string += new CustomerHelper(il.getOutletId()).getName();
					string += "\n";
				}
				dates = il.getDates();
				break;
			case "Invoicing Discrepancies":
				dates = ((InvoiceDiscrepancy) report).getDates();
				string = report.getModule();
				break;
			case "Outlet List":
				OutletList ol = (OutletList) report;
				string += new ItemHelper().getFamilyName(ol.getProductLineId());
				string += " sold by ";
				string += new Route(ol.getRouteId()).getName();
				string += "\n";
				dates = ol.getDates();
				break;
			case "Overdue Invoices":
				Overdue od = (Overdue) report;
				dates = new Date[0];
				string = new CustomerHelper(od.getCustomerId()).getName()
						+ " is on hold\nuntil the following are paid";
				break;
			case "Overdue Statement":
				dates = new Date[0];
				string = new CustomerHelper(
						((OverdueStatement) report).getCustomerId()).getName();
				break;
			case "Receiving Report List":
				ReceivingList rl = (ReceivingList) report;
				string += new ItemHelper().getName(rl.getItemId());
				if (rl.getRouteId() != null)
					string += "\nback-loaded from "
							+ new Route(rl.getRouteId()).getName();
				else
					string += "\nreturned/purchased";
				string += "\n";
				dates = rl.getDates();
				break;
			case "Route Report":
				RouteReport rr = (RouteReport) report;
				dates = rr.getDates();
				string = new Route(rr.getRouteId()).getName();
				break;
			case "Sales Order List":
				SalesOrderList sol = (SalesOrderList) report;
				string += new ItemHelper().getName(sol.getItemId());
				string += "\nordered by ";
				string += new Route(sol.getRouteId()).getName();
				string += "\n";
				dates = sol.getDates();
				break;
			case "Sales Report":
				SalesReport sr = (SalesReport) report;
				string = new ItemHelper().getFamilyName(sr.getCategoryId());
				string = (sr.getMetric().equals("SALES TO TRADE") ? "Sales to Trade of "
						: "Productivity for ")
						+ string;
				dates = sr.getDates();
				break;
			case "Stock Take":
				StockTake st = (StockTake) report;
				string += "Summary of Count Conducted";
				dates = new Date[] { st.getPostDate() };
				break;
			case "Stock Take ":
				StockTakeVariance stv = (StockTakeVariance) report;
				string += "Variance of System Inventory vs. Count Conducted";
				dates = new Date[] { stv.getDates()[1] };
				break;
			case "Value-Added Tax":
				dates = ((Vat) report).getDates();
				string = report.getModule();
				break;
			default:
				new ErrorDialog("" + "ReportHeaderBar\n"
						+ "has no option for\n" + report.getModule());
				break;
		}
		Label lbl = new Label(parent, SWT.CENTER);
		int length = dates.length;
		if (length != 0) {
			start = DIS.LDF.format(dates[0]);
			if (length > 1) {
				end = DIS.LDF.format(dates[1]);
				string += !start.equals(end) ? (" from " + start + " to " + end)
						: (" on " + start);
			} else {
				string += " on " + start;
			}
		}
		report.setHeader(string);
		lbl.setText(string);
		lbl.setFont(View.boldFont());
		GridData gd = new GridData();

		gd.horizontalAlignment = GridData.CENTER;
		lbl.setLayoutData(gd);
	}
}
