package ph.txtdis.windows;

import org.eclipse.swt.widgets.Composite;

public class PostButton extends FocusButton {
	private ReportView view;

	public PostButton(Composite parent, ReportView reportView, Report report) {
		super(parent, report, "Database", "Save " + report.getModule());
		this.view = reportView;
		getButton().setEnabled(false);
	}

	@Override
	protected void open() {
		getButton().setEnabled(false);
		switch (module) {
			case "Sales Target":
				Program p = (Program) report;
				ProgramView pv = (ProgramView) view;
				p = new ProgramSaving(pv, p).get();
				if(p != null)
					if (new ProgramPosting().set(p))
						pv.getTxtProgramId().setText("" + p.getProgramId()); 
				break;
			case "Customer Data":
				CustomerMaster cm = (CustomerMaster) report;
				CustomerView cv = (CustomerView) view;
				cm = new CustomerSaver(cv, cm).get();
				if(cm != null)
					if (new CustomerPosting().set(cm))
						cv.getTxtId().setText("" + cm.getId()); 
				break;
			case "Item Data":
				ItemMaster im = (ItemMaster) report;
				ItemView iv = (ItemView) view;
				im = new ItemSaving(iv, im).get();
				if(im != null)
					if (new ItemPosting().set(im))
						iv.getTxtId().setText("" + im.getId()); 
				break;
			case "Invoice": 				
				new InvoicePosting().set((Order) report);
				break;
			case "Delivery Report": 				
				if(new DeliveryPosting().set((Order) report)) 
					new DeliveryView(((Order) report).getId());
				break;
			case "Purchase Order":
				if(new PurchaseOrderPosting().set((Order) report)) 
					new PurchaseOrderView(((Order) report).getId());
				break;
			case "Sales Order":
				if(new SalesOrderPosting().set((Order) report)) 
					new SalesOrderView(((Order) report).getId());
				break;
			case "Stock Take":
				StockTake st = (StockTake) report;
				StockTakeView stv = (StockTakeView) view;
				// dispose the last table item
				stv.getTableItem(stv.getTable().getItems().length - 1).dispose();
				stv.getBtnItemId().dispose();
				stv.getTxtItemId().dispose();
				st = new StockTakeSaving(stv, st).get();
				if (new StockTakePosting().set(st))
					stv.getTxtOrderId().setText("" + st.getId()); 
				break;
			case "Remittance":
				Remittance remit = (Remittance) report;
				RemittanceView rView = (RemittanceView) view;
				if (new RemittancePosting().set(remit)) 
					rView.getTxtRemitId().setText("" + remit.getRemitId());					
				break;
			case "Receiving Report":
				Receiving rr = (Receiving) report;
				ReceivingView rrView = (ReceivingView) view;
				if(new ReceivingPosting().set(rr))
					rrView.getTxtOrderId().setText("" + rr.getRrId());
				break;
			default:
				new ErrorDialog("No Post Button option\nfor " + module);
				return;
		}
	}
}

