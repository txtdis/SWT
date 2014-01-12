package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Calendar;

import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.swt.widgets.Composite;

public class VarianceButton extends ReportButton {
	
	private Date[] dates;
	private LoadSettlement loadSettlement;

	public VarianceButton(Composite parent, Report report) {
		super(parent, report, "Percent", "Show Variance");
	}

	@Override
	protected void doWhenSelected() {
		final Route route = new Route();
		final String[] routes = route.getList();
		dates = new Date[] {DIS.CLOSED_DSR_BEFORE_SO_CUTOFF, report.getDate()};
        for (final String routeName : routes) {
            new ProgressDialog() {
				@Override
				public void proceed() {
					int routeId = route.getId(routeName);
					loadSettlement = new LoadSettlement(dates, routeId);						}
			};
            if(loadSettlement.getTotalVariance().compareTo(BigDecimal.ZERO) != 0) {
				new ErrorDialog("Complete all Load Settlements\nbefore making any comparison");
				new SettlementView(loadSettlement);
				return;
            }
        }
		StockTake stockTake = (StockTake) report;
		Date startDate, endDate;
		Calendar today = Calendar.getInstance();
		DateUtils.truncate(today, Calendar.DAY_OF_MONTH);
		Calendar lastSaturday = Calendar.getInstance();
		DateUtils.truncate(lastSaturday, Calendar.DAY_OF_MONTH);
		Calendar saturdayBeforeLast = Calendar.getInstance();
		DateUtils.truncate(saturdayBeforeLast, Calendar.DAY_OF_MONTH);
		Calendar lastDayOfPreviousMonth = Calendar.getInstance();
		DateUtils.truncate(lastDayOfPreviousMonth, Calendar.DAY_OF_MONTH);
		Calendar lastDayOfThisMonth = Calendar.getInstance();
		DateUtils.truncate(lastDayOfThisMonth, Calendar.DAY_OF_MONTH);
		lastSaturday.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		lastSaturday.add(Calendar.WEEK_OF_YEAR, -1);
		if (today.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
			saturdayBeforeLast.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
			saturdayBeforeLast.add(Calendar.WEEK_OF_YEAR, -2);
		}
		lastDayOfPreviousMonth.add(Calendar.MONTH, -1);
		lastDayOfPreviousMonth.set(Calendar.DAY_OF_MONTH,
		        lastDayOfPreviousMonth.getActualMaximum(Calendar.DAY_OF_MONTH));
		lastDayOfThisMonth.set(Calendar.DAY_OF_MONTH, today.getActualMaximum(Calendar.DAY_OF_MONTH));
		endDate = new Date(today.getTimeInMillis());
		if (today.equals(lastDayOfThisMonth)) {
			startDate = new Date(lastSaturday.getTimeInMillis());
		} else {
			if (lastDayOfPreviousMonth.after(lastSaturday) && lastDayOfPreviousMonth.before(today)) {
				startDate = new Date(lastDayOfPreviousMonth.getTimeInMillis());
			} else {
				if (today.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
					startDate = new Date(lastSaturday.getTimeInMillis());
				} else {
					startDate = new Date(saturdayBeforeLast.getTimeInMillis());
					endDate = new Date(lastSaturday.getTimeInMillis());
				}
			}
		}
		dates = new Date[] { startDate, endDate };
		dates = new CalendarDialog(dates).getDates();
		startDate = dates[0];
		endDate = dates[1];
		if (!stockTake.isDone(startDate) || !stockTake.isDone(endDate)) {
			new ErrorDialog("No stock take done on\n" + DIS.LONG_DATE.format(startDate) + "\n" + "and/or\n"
			        + DIS.LONG_DATE.format(endDate) + "\n");
			new StockTakeView(endDate);
		} else if (!stockTake.isDataEntryClosed(startDate) || !stockTake.isDataEntryClosed(endDate)) {
			new ErrorDialog("Stock take must be tagged as complete\nbefore any comparison is made\n"
			        + "\nPlease click the \"Hand/Stop\" icon to do so");
			new StockTakeView(endDate);
		} else

			new StockTakeView(dates);
	}
}
