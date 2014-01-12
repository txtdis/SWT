package ph.txtdis.windows;

import java.sql.Date;

public interface DataEntryClosable {
	public void closeDataEntry();
	public boolean isDataEntryClosed(Date date);
}
