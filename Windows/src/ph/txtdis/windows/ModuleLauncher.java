package ph.txtdis.windows;

import java.sql.Date;

public class ModuleLauncher {
	private String columnDatum;

	public ModuleLauncher(Data data, int rowIdx, int colIdx) {
		Object[][] tableData = data.getTableData();
		System.out.println(tableData[rowIdx][colIdx]);
		System.out.println(data.getClass().getName());

/**		
		// Get Row ID
		String strId = table.getItem(rowIdx).getText(type == Type.REMIT ? 2 : 1);
		if (strId.isEmpty())
			strId = "0";
		id = Integer.parseInt(strId.replace("(", "-").replace(")", ""));
		// Get Column Text
		String colDatum;
		switch (data.getType()) {
		case CUSTOMER_LIST:
		case INVENTORY:
		case ITEM_LIST:
		case RECEIVABLES:
		case RECEIVING_LIST:
		case SETTLEMENT:
		case SALES_LIST:
		case SALES_REPORT:
		case COUNT_VARIANCE:
		case TARGET:
		case TRANSMIT:
		case OUTLET_LIST:
			new ModuleLauncher(data, id, colIdx);
			break;
		case OVERDUE:
		case VAT:
		case INVOICE_DELIVERY_LIST:
			colDatum = table.getItem(rowIdx).getText(2);
			new ModuleLauncher(data, id, colDatum);
			break;
		case REMIT:
			colDatum = table.getItem(rowIdx).getText(1);
			new ModuleLauncher(data, id, colDatum);
			break;
		case DELIVERY:
		case PURCHASE:
		case SALES:
		case INVOICE:
			orderView = (OrderView) view;
			order = (OrderData) data;
			if (isPostingButtonEnabled(orderView, rowIdx)) {
				orderView.setRowIdx(rowIdx);
				new ItemIdInputSwitcher(orderView, order);
			}
			break;
		case COUNT:
			colDatum = table.getItem(rowIdx).getText(3);
			new ModuleLauncher(data, id, colIdx, colDatum);
			break;
		default:
			new InfoDialog("Double-click");
		}

		
		
		columnDatum = colDatum;
		String module = data.getModule();
		String orderType = "";
		Date[] dates = data.getDates();
		Date date = data.getDate();
		int routeId;
		switch (module) {
		case "Customer List":
			new CustomerView(rowIdx);
			break;
		case "Remittance Settlement":
			setLineItemsAsRemittances();
		case "Remittance":
			if (isLineItemARemittance()) {
				new RemitView(new RemitData(rowIdx));
				break;
			}
		case "Cash Settlement":
		case "Invoice/Delivery List":
		case "Invoicing Discrepancies":
		case "Overdue Statement":
		case "Value-Added Tax":
			if (rowIdx < 0) {
				new DeliveryView(new DeliveryData(-rowIdx));
			} else {
				String series = colDatum == null || colDatum.isEmpty() ? " " : colDatum;
				new InvoiceView(rowIdx, series);
			}
			break;
		case "Item List":
		case "Inventory":
			new ItemView(new ItemData(rowIdx));
			break;
		case "Outlet List":
			OutletList outletList = (OutletList) data;
			dates = data.getDates();
			int outletId = rowIdx;
			int categoryId = outletList.getCategoryId();
			int productLineId = outletList.getProductLineId();
			new OrderListView("sold", dates, outletId, productLineId, categoryId);
			break;
		case "Receiving Report List":
			new ReceivingView(rowIdx);
			break;
		case "Receivables":
			new OverdueStatementView(rowIdx);
			break;
		case "Load Settlement":
			dates = data.getDates();
			routeId = ((LoadSettlement) data).getRouteId();
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
				String name = Route.getName(rowIdx);
				routeId = new Location(name).getId();
				break;
			default:
				return;
			}
			int itemId = rowIdx;
			new OrderListView(orderType, dates, itemId, routeId, null);
			break;
		case "Sales Order List":
			new SalesView(rowIdx);
			break;
		case "Sales Report":
			if (colIdx < 4)
				new InfoDialog("Choose any column\n on the right of TOTAL");
			else {
				SalesReport salesReport = (SalesReport) data;
				dates = salesReport.getDates();
				categoryId = salesReport.getCategoryId();
				String[] productLines = Item.getProductLines(categoryId);
				productLineId = Item.getFamilyId(productLines[colIdx - 4]);
				routeId = outletId = rowIdx;
				if (salesReport.isPerRoute())
					new OrderListView("outlet", dates, routeId, productLineId, categoryId);
				else
					new OrderListView("sold", dates, outletId, productLineId, categoryId);
			}
			break;
		case "Stock Take":
			dates = new Date[] {date};
			itemId = rowIdx;
			new OrderListView("count", dates, itemId, null, null);
			break;
		case "Stock Take Reconciliation":
			boolean shouldListBeViewed = true;
			CountVariance countVariance = (CountVariance) data;
			dates = countVariance.getDates();

			switch (colIdx) {
			case 4:
				orderType = "count";
				dates = new Date[] { dates[0], dates[0] };
				break;
			case 5:
				orderType = "receiving";
				break;
			case 6:
				orderType = "sales";
				break;
			case 7:
				orderType = "count";
				dates = new Date[] { dates[1], dates[1] };
				break;
			default:
				shouldListBeViewed = false;
				new ItemView(rowIdx);
			}
			if (shouldListBeViewed) {
				itemId = rowIdx;
				int qcId = colDatum.equals("GOOD") ? 0 : 2;
				new OrderListView(orderType, dates, itemId, null, qcId);
			}
			break;
		case "Stock Take Tag List":
			new CountView(rowIdx);
			break;
		case "Target Lt":
			new SalesTargetView(rowIdx);
			break;
		case "Transmittal":
			new RemitView(rowIdx);
			break;
		default:
			new InfoDialog("@" + module);
		}
	}

	private boolean isLineItemARemittance() {
		return columnDatum.equals("R");
	}

	private void setLineItemsAsRemittances() {
		columnDatum = "R";
	}
	
	**/
	}
}
