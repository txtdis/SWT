package ph.txtdis.windows;

import java.sql.Date;
import java.util.Calendar;

import org.apache.commons.lang3.time.DateUtils;

public class DBMSPreConnCheck {
	int serverVersion;
	
	public boolean isOK() {
		return isVersionLatest() && isTimeZoneCorrect() && isDateCorrect();
	}


	private boolean isVersionLatest() {
		serverVersion = Integer.parseInt(DIS.SERVER_VERSION.replace(".", ""));
		int inUse = Integer.parseInt(Login.version().replace(".", ""));
		if (serverVersion > inUse) {
			new ErrorDialog("Use latest version:\n" + DIS.SERVER_VERSION);			
			return false;
		} else if (serverVersion < inUse) {
			new DatabaseUpdater().patch();
			return false;
		}
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
		Date dateOnServer = DIS.TODAY;
		Date dateOnPC = new Date(Calendar.getInstance().getTimeInMillis());
		if (!DateUtils.isSameDay(dateOnPC, dateOnServer)) {
			new ErrorDialog("Correct PC date to\n" + dateOnServer);
			return false;
		}
		return true ;
	}
}
