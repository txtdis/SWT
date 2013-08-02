
package ph.txtdis.windows;

import java.sql.Date;
import java.text.ParseException;

public class ProgramHelper {
	public final String ERROR = "" +
			" is already inclusive\n" +
			"in a previous incentive's date range";

	public ProgramHelper() {
	}
		
	public boolean hasDateBeenUsed(int categoryId, String strDate) {
		try {
			Date date =	new Date(DIS.POSTGRES_DATE.parse(strDate).getTime());
			Object[] ao = new Data().getData(categoryId, "" +
					"SELECT	min(start_date)," +
					"		max(end_date) " +
					"FROM	target_header " +
					"WHERE	category_id = ? " +
					"");
			if(ao[0] != null) {
				Date start = (Date) ao[0];
				Date end = (Date) ao[1];
				if(start.compareTo(date) <= 0 && date.compareTo(end) <= 0) {
					return true;
				} 
			}
			return false;
		} catch (ParseException e) {
			new ErrorDialog(e);
			return true;
		}
	}
}
