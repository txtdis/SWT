package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

public class MenuButton {

	public MenuButton(Composite parent, final String name) {
		final Composite child = new Composite(parent, SWT.NO_TRIM);
		child.setLayout(new GridLayout(1, false));

		final Button button = new Button(child, SWT.FLAT);
		button.setImage(new Image(child.getDisplay(), this.getClass().getResourceAsStream(
				"images/" + name.replace("\n", "").replace("/", "").replace(" ", "") + ".png")));
		GridData gdButton = new GridData();
		gdButton.verticalAlignment = GridData.FILL;
		gdButton.horizontalAlignment = GridData.CENTER;
		gdButton.grabExcessVerticalSpace = true;
		button.setLayoutData(gdButton);

		button.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				child.getShell().dispose();
				// @sql:on
				switch (name) {
				// Main Menu
				case "Lists": new ListMenu(); break;
				case "Supply Chain": new SupplyChainMenu(); break;			
				case "Sales": new SalesMenu(); break;
				case "Finance": new FinanceMenu(); break;
				case "Systems": new SystemsMenu(); break;
				// Lists Menu
				case "Partner": new CustomerListView(""); break;
				case "Stock": new ItemListView(""); break;
				case "Transaction": new ListMenu(); break;
				case "Contact": new ListMenu(); break;
				case "Discrepancy": new DiscrepancyMenu(); break;
				// Supply Chain
				case "Purchases": new PurchaseOrderView(0); break;			
				case "Receipts": new ReceivingView(0); break;			
				case "Inventory": new InventoryView(""); break;			
				case "Shipment": new DeliveryView(0); break;			
				case "Stock Take": new StockTakeView(DIS.YESTERDAY); break;
				// Sales Menu
				case "Price": new PricelistView(); break;
				case "Remittance": new RemittanceView(0); break;
				case "Sales Order": new SalesOrderView(0); break;
				case "Invoice": new InvoiceView(0); break;
				case "Reports": new SalesReportView(null, "SALES TO TRADE", -10, false); break;
				// Finance Menu
				case "Account\nReceivables": new ReceivablesView(); break;
				case "Account\nPayables": new FinanceMenu(); break;
				case "Credit/Debit\nMemos": new FinanceMenu(); break;
				case "VAT\n ": new VatView(null); break;
				case "Discrepancy\n ": new DiscrepancyMenu(); break;
				// System Menu
				case "Backup": new Backup(); break;
				case "Restore": new SystemsMenu(); break;
				case "Settings": new SystemsMenu(); break;
				case "SMS": new SystemsMenu(); break;
				case "Review": new IrregularListView(null, ""); break;
				// Discrepancy Menu
				case "Purchasing": new DiscrepancyMenu(); break;
				case "Receiving": new SettlementView(new LoadSettlement(null, 0)); break;
				case "Physical Count": new DiscrepancyMenu(); break;
				case "Invoicing": new SettlementView(new DepositSettlement(null, 0)); break;
				case "Collection": new SettlementView(new CashSettlement(null, 0)); break;
				// @sql:off
				}
			}
		});

		button.addListener(SWT.Dispose, new Listener() {
			@Override
			public void handleEvent(Event e) {
				button.getImage().dispose();
			}
		});

		Label label = new Label (child, SWT.CENTER);
		label.setText(name);
		GridData gdLabel = new GridData();
		gdLabel.verticalAlignment = GridData.FILL;
		gdLabel.horizontalAlignment = GridData.CENTER;
		gdLabel.grabExcessVerticalSpace = true;
		label.setLayoutData(gdLabel);
	}
}
