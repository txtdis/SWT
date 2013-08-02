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
		Date[] dates;
		int categoryId;
		int routeId;
		int itemId = rowIdx;
		int outletId = rowIdx;
		switch (module) {
			case "Customer List":
				new CustomerView(rowIdx); break;
			case "Invoice/Delivery List":
			case "Invoicing Discrepancies":
			case "Remittance":
			case "Overdue Statement":
			case "Value-Added Tax":
				if (rowIdx < 0) {
					new DeliveryView(-rowIdx) {
						@Override
						protected String getModule() {
							return "Delivery Report ";
						}						
					}; 
					break;				
				} else {
					new InvoiceView(rowIdx, colDatum){
						@Override
						protected String getModule() {
							return "Invoice ";
						}
					}; 
					break;
				}
			case "Item List":
				new ItemView(rowIdx); break;
			case "Outlet List":
				OutletList ol = (OutletList) report;
				dates = ol.getDates();
				categoryId = ol.getCategoryId();
				int productLineId = ol.getProductLineId();
				new InvoiceDeliveryListView(dates, outletId, productLineId, categoryId); 
				break;
			case "Receiving Report List":
				new ReceivingView(rowIdx); break;
			case "Receivables":
				new OverdueStatementView(rowIdx); break;
			case "Loaded Material Balance":
				LoadedMaterialBalance lmb = (LoadedMaterialBalance) report;
				dates = lmb.getDates();
				routeId = lmb.getRouteId();
				switch (colIdx) {			
					case 3: new SalesOrderListView(dates, itemId, routeId); break;
					case 4: new InvoiceDeliveryListView(dates, itemId, routeId, null); break;
					case 5: new ReceivingListView(dates, itemId, routeId); break;
					default: break;
				} 
				break;
			case "Sales Order List":
				new SalesOrderView(rowIdx); break;
			case "Sales Report":
				if (colIdx < 4) {
					new InfoDialog("Choose any column\non the right of TOTAL");
					break;
				}
				SalesReport sr = (SalesReport) report;
				dates = sr.getDates();
				categoryId = sr.getCategoryId();
				routeId = rowIdx;
				int grp = sr.getRouteOrOutlet();
				ItemHelper ih = new ItemHelper();
				String[] productLines = ih.getProductLines(categoryId);
				productLineId = ih.getFamilyId(productLines[colIdx - 4]);
				if (grp != DIS.ROUTE) {
					new InvoiceDeliveryListView(
							dates, outletId, productLineId, categoryId);
				} else {
					new OutletListView(dates, routeId, productLineId, categoryId); 
				}
				break;
			case "Stock Take ":
				StockTakeVariance stv = (StockTakeVariance) report;
				dates = stv.getDates();
				switch (colIdx) {
					case 3:
						new StockTakeListView(dates[0], itemId);
						break;
					case 4: 
						new ReceivingListView(dates, itemId, null); 
						break;
					case 5: 
						new InvoiceDeliveryListView(dates, itemId, null, null); 
						break;
					case 6:
						new StockTakeListView(dates[1], itemId);
						break;
					default:
						if(Login.getGroup().equals("super_supply") || Login.getGroup().equals("sys_admin"))
							new StockTakeAdjustmentDialog(stv, itemId); 
				} 
				break;
			case "Stock Take Tag List":
				new StockTakeView(rowIdx); break;
			case "Target List":
				new ProgramView(rowIdx); break;
			default:
				new InfoDialog("@" + module);
		}
	}
}
