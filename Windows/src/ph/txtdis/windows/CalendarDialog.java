package ph.txtdis.windows;

import java.sql.Date;
import java.util.Calendar;

import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Shell;

public class CalendarDialog extends DialogView {
	private DateTime startDialog, endDialog;
	private Calendar startCalendar, endCalendar;
	private Date[] dates;
	private Date date, startDate, endDate;
	private int length;
	private boolean shouldAllShellsBeDisposed;

	public CalendarDialog(Date[] dates) {
		this(dates, true);
	}

	public CalendarDialog(Date[] dates, boolean shouldAllShellsBeDisposed) {
		if (dates == null) {
			length = 1;
			date = DIS.TODAY;
			dates = new Date[] {
				date };
		} else {
			length = dates.length;
			date = dates[0];
		}
		this.dates = dates;
		this.shouldAllShellsBeDisposed = shouldAllShellsBeDisposed;
		setName(length > 1 ? "Choose Start and End Dates" : "Choose Date");
		open();
	}

	@Override
	protected void setLeftPane() {
		startDialog = new DateTime(header, SWT.CALENDAR | SWT.BORDER);
		startCalendar = Calendar.getInstance();
		startCalendar.setTime(dates[0]);
		startCalendar.add(Calendar.DAY_OF_MONTH, -1);
		startDialog.setDate(startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH),
				startCalendar.get(Calendar.DAY_OF_MONTH));
	}

	@Override
	protected void setRightPane() {
		if (length > 1) {
			endDialog = new DateTime(header, SWT.CALENDAR | SWT.BORDER);
			endCalendar = Calendar.getInstance();
			endCalendar.setTime(dates[1]);
			endCalendar.add(Calendar.DAY_OF_MONTH, 1);
			endDialog.setDate(endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH),
			        endCalendar.get(Calendar.DAY_OF_MONTH));
		}
	}

	@Override
	protected void setOkButtonAction() {
		startCalendar.set(startDialog.getYear(), startDialog.getMonth(), startDialog.getDay());
		startDate = new Date(startCalendar.getTimeInMillis());
		if (length > 1) {
			endCalendar.set(endDialog.getYear(), endDialog.getMonth(), endDialog.getDay());
			endDate = new Date(endCalendar.getTimeInMillis());
			if (endDate.before(startDate)) {
				new ErrorDialog("Must not be after\n" + DIS.POSTGRES_DATE.format(startDate));
				dates = null;
			} else {
				dates[1] = endDate;
			}
		}
		dates[0] = startDate;
		if (shouldAllShellsBeDisposed) {
			for (Shell shell : UI.DISPLAY.getShells()) {
				shell.dispose();
			}
		} else {
			shell.dispose();
		}
	}

	public Date[] getDates() {
		return dates;
	}

	public Date getDate() {
		return dates == null ? null : dates[0];
	}

	public boolean isEqual() {
		if (isCancelled || !DateUtils.isSameDay(date, dates[0])) {
			new ErrorDialog("Entered and clicked dates\ndo not match; try again.");
			return false;
		} else {
			return true;
		}
	}
}
