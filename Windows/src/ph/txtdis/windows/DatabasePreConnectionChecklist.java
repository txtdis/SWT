package ph.txtdis.windows;

import java.sql.Date;
import java.sql.SQLException;
import java.util.Calendar;

import org.apache.commons.lang3.time.DateUtils;

public class DatabasePreConnectionChecklist {
	int serverVersion;
	
	public boolean isOK() {
		return isVersionLatest() && isTimeZoneCorrect() && isDateCorrect();
	}


	private boolean isVersionLatest() {
		serverVersion = DIS.SERVER_VERSION == null ? 0 : Integer.parseInt(DIS.SERVER_VERSION.replace(".", ""));
		System.out.println(DIS.SERVER_VERSION);
		int inUse = Integer.parseInt(DIS.CLIENT_VERSION.replace(".", ""));
		if (serverVersion > inUse) {
			new ErrorDialog("Use latest version:\n" + DIS.SERVER_VERSION);			
			return false;
		} else if (serverVersion < inUse)
			updateVersion();
		return true;

	}

	private boolean isTimeZoneCorrect() {
		int timezoneOnPC = Calendar.getInstance().getTimeZone().getRawOffset() / 1000 / 60 / 60;
		if (timezoneOnPC != DIS.SERVER_TIMEZONE) {
			new ErrorDialog("Correct PC timezone to\nUTC" + (DIS.SERVER_TIMEZONE > 0 ? "+" : "") + DIS.SERVER_TIMEZONE);
			return false;
		}
		return true ;
	}

	private boolean isDateCorrect() {
		Date today = new Date(Calendar.getInstance().getTimeInMillis());
		if (!DateUtils.isSameDay(today, DIS.TODAY)) {
			new ErrorDialog("Correct PC date to\n" + DIS.TODAY);
			return false;
		}
		return true ;
	}

	private void updateVersion() {
		new Posting(new SalesOrder()) {
			protected void postData() throws SQLException {
				String stmt = "UPDATE version SET latest = ? ";
				ps = conn.prepareStatement(stmt);
				ps.setString(1, DIS.CLIENT_VERSION);
				ps.executeUpdate();
			}
		}.wasCompletedWithoutCheck();
	}
}
