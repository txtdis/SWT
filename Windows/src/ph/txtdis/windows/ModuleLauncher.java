package ph.txtdis.windows;

import java.sql.Date;

public class ModuleLauncher {
	public ModuleLauncher(Report report, int rowIdx, String colDatum) {
		this(report, rowIdx, 2, colDatum);
	}

	public ModuleLauncher(Report report, int rowIdx, int colIdx) {
		this(report, rowIdx, colIdx, null);
	}

	public ModuleLauncher(Report report, int rowIdx, int colIdx, String colDatum) {
		String module = report.getModule();
		String orderType = "";
		Date[] dates;
		int categoryId;
		int routeId = rowIdx;
		int itemId = rowIdx;
		int outletId = rowIdx;
		switch (module) {
			case "Customer List":
				new CustomerView(rowIdx);
				break;
			case "Invoice/Delivery List":
			case "Invoicing Discrepancies":
			case "Remittance":
			case "Overdue Statement":
			case "Value-Added Tax":
				if (rowIdx < 0) {
					int deliveryId = -rowIdx;
					new DeliveryView(deliveryId) {
						@Override
						protected String getModule() {
							return "Delivery Report ";
						}
					};
					break;
				} else {
					int invoiceId = rowIdx;
					String series = colDatum;
					new InvoiceView(invoiceId, series) {
						@Override
						protected String getModule() {
							return "Invoice";
						}
					};
					break;
				}
			case "Item List":
				new ItemView(rowIdx);
				break;
			case "Outlet List":
				OutletList outletList = (OutletList) report;
				dates = outletList.getDates();
				categoryId = outletList.getCategoryId();
				int productLineId = outletList.getProductLineId();
				new OrderListView("sold", dates, outletId, productLineId, categoryId);
				break;
			case "Receiving Report List":
				new ReceivingView(rowIdx);
				break;
			case "Receivables":
				new OverdueStatementView(rowIdx);
				break;
			case "Load-In/Out Settlement":
				LoadSettlement loadedMaterialBalance = (LoadSettlement) report;
				dates = loadedMaterialBalance.getDates();
				routeId = loadedMaterialBalance.getRouteId();
				switch (colIdx) {
					case 3:
						orderType = "sales";
						break;
					case 4:
						orderType = "sold";
						break;
					case 5:
						orderType = "receiving";
						break;
					case 6: 
						orderType = "count";
						String locationIsAnExTruckRoute = new Route().getName(routeId);
						routeId = new Location(locationIsAnExTruckRoute).getId();
						break;
					default:
						return;
				}
				new OrderListView(orderType, dates, itemId, routeId, null);
				break;
			case "Sales Order List":
				new SalesOrderView(rowIdx);
				break;
			case "Sales Report":
				if (colIdx < 4) {
					new InfoDialog("Choose any column\non the right of TOTAL");
					break;
				}
				SalesReport salesReport = (SalesReport) report;
				dates = salesReport.getDates();
				categoryId = salesReport.getCategoryId();
				ItemHelper ih = new ItemHelper();
				String[] productLines = ih.getProductLines(categoryId);
				productLineId = ih.getFamilyId(productLines[colIdx - 4]);
				if (salesReport.isPerRoute()) {
					new OrderListView("outlet", dates, routeId, productLineId, categoryId);
				} else {
					new OrderListView("sold", dates, outletId, productLineId, categoryId);
				}
				break;
			case "Stock Take ":
				boolean shouldListBeViewed = true;
				StockTakeVariance stockTakeVariance = (StockTakeVariance) report;
				dates = stockTakeVariance.getDates();
				switch (colIdx) {
					case 4:
						orderType = "count";
						dates = new Date[] { dates[0] };
						break;
					case 5:
						orderType = "receiving";
						break;
					case 6:
						orderType = "sales";
						break;
					case 7:
						orderType = "count";
						dates = new Date[] {dates[1] };
						break;
					default:
						if (Login.getGroup().equals("super_supply") || Login.getGroup().equals("sys_admin"))
							new StockTakeAdjustmentDialog(stockTakeVariance, itemId);
						shouldListBeViewed = false;
				}
				if (shouldListBeViewed) {
					int qcId = colDatum.equals("GOOD") ? 0 : 2; 
					new OrderListView(orderType, dates, itemId, null, qcId);
				}
				break;
			case "Stock Take Tag List":
				new StockTakeView(rowIdx);
				break;
			case "Target Lt":
				new SalesTargetView(rowIdx);
				break;
			default:
				new InfoDialog("@" + module);
		}
	}
}
