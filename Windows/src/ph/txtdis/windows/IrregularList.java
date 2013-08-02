package ph.txtdis.windows;

import java.sql.Date;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;

public class IrregularList extends Report {

	public IrregularList(Date[] dates, String string) {
		Calendar cal = Calendar.getInstance();
		if (dates == null) {
			dates = new Date[2];
			cal.set(2013, Calendar.JANUARY, 1);
			dates[0] = new Date(cal.getTimeInMillis());
			dates[1]= new Date(Calendar.getInstance().getTimeInMillis());
		}
		
		module = "Irregular Activities";
		headers = new String[][] {
				{StringUtils.center("TIMESTAMP", 23), "Date"},
				{StringUtils.center("USER", 10), "String"},
				{StringUtils.center("ACTIVITY", 80), "String"},
		};

		data = new Data().getDataArray(dates, "" +
				"SELECT	il.time_stamp, " +
				"		cd.name, " +
				"		il.activity " +
				"FROM 	irregular_log AS il, " +
				"		system_user AS su, " +
				"		contact_detail AS cd " +
				"WHERE	su.system_id = il.user_id " +
				"	AND	cd.id = su.contact_id " +
				"	AND il.time_stamp BETWEEN ? AND ? " +
				"	AND	il.activity LIKE '%" + string.toUpperCase() + "%' " +
				"ORDER 	BY il.time_stamp "
				);
	}
}
