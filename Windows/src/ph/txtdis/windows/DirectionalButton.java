package ph.txtdis.windows;

import java.sql.Date;
import java.util.Calendar;

import org.eclipse.swt.widgets.Composite;

public abstract class DirectionalButton extends FocusButton {
	protected int increment;
	private Date[] dates;
	private Calendar start, end;

	public DirectionalButton(Composite parent, Report report, String icon, String tooltip) {
		super(	parent, 
				report,
				icon,
				tooltip
				);
	}

	@Override
	public void doWhenSelected() {
		setIncrement();
		start = Calendar.getInstance();
		end = Calendar.getInstance();
		dates = new Date[] {
				new Date(start.getTimeInMillis()), 
				new Date(end.getTimeInMillis())};
		switch (module) {
			case "Loaded Material Balance":
				LoadedMaterialBalance lmb = (LoadedMaterialBalance) report;
				dates = lmb.getDates();
				int routeId = lmb.getRouteId();
				incrementDaily();
				new LoadedMaterialBalanceView(dates, routeId);
				break;
			case "Invoicing Discrepancies":
				dates = ((InvoiceDiscrepancy) report).getDates();
				incrementDaily();
				new InvoiceDiscrepancyView(dates);
				break;
			case "Value-Added Tax":
				dates = ((Vat) report).getDates();
				incrementMonthly();
				new VatView(dates);
				break;
			case "Sales Report":
				dates = ((SalesReport) report).getDates();
				incrementMonthly();
				String metric = ((SalesReport) report).getMetric();
				int cat = ((SalesReport) report).getCategoryId();
				int grp = ((SalesReport) report).getRouteOrOutlet();
				new SalesReportView(dates, metric, cat, grp);
				break;
		}
	}

	protected void setIncrement() {
		increment = -1;
	}

	private void incrementMonthly() {
		end.setTime(dates[1]);
		end.add(Calendar.MONTH, increment);
		end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH));
		start.set(end.get(Calendar.YEAR), end.get(Calendar.MONTH), 1);
		dates[0] = new Date(start.getTimeInMillis());
		dates[1] = new Date(end.getTimeInMillis());
		parent.getShell().dispose();
	}

	private void incrementDaily() {
		start.setTime(dates[0]);
		end.setTime(dates[0]);
		start.add(Calendar.DAY_OF_MONTH, increment);
		end.add(Calendar.DAY_OF_MONTH, increment);
		dates[0] = new Date(start.getTimeInMillis());
		dates[1] = new Date(end.getTimeInMillis());
		parent.getShell().dispose();
	}
}

