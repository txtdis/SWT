package ph.txtdis.windows;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
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
		int inUse = Integer.parseInt(DIS.CLIENT_VERSION.replace(".", ""));
		if (serverVersion > inUse) {
			new ErrorDialog("Use latest version:\n" + DIS.SERVER_VERSION);			
			return false;
		} else if (serverVersion < inUse) {
			updateVersion();
			updateDatabase();
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

	private void updateDatabase() {
		Database.getInstance().closeConnection();
		Connection conn = Database.getInstance().getConnection("postgres", "postgres", "mgdc_smis");
		PreparedStatement ps = null;
		String msg = "\nPlease log-in again.";
		try {
			conn.setAutoCommit(false);
			ps = conn.prepareStatement(""
					// @sql:on
					+ "GRANT SELECT\n" 
					+ "   ON ALL TABLES IN SCHEMA public\n" 
					+ "   TO guest;\n" 
					// @sql:off

//					+ "DELETE FROM delivery_header WHERE delivery_id = 664;\n" 

//					+ "UPDATE default_date\n" 
//					+ "   SET value = '2013-12-08'\n" 
//					+ " WHERE name = $$CLOSED-DSR-BEFORE-S/O CUTOFF$$;\n" 
//					+ "DELETE FROM delivery_header WHERE delivery_id = 303;\n" 
					
//					+ "DROP ROLE IF EXISTS	marivic;\n" 
//					+ "CREATE ROLE \"marivic\" LOGIN PASSWORD 'marvic' NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE;\n" 
//					+ "GRANT user_finance TO marivic;\n"
					
//					+ "GRANT INSERT\n" 
//					+ "   ON count_adjustment,\n" 
//					+ "      count_header\n" 
//					+ "   TO user_finance;\n" 
//					+ "REVOKE INSERT\n" 
//					+ "   ON count_adjustment,\n" 
//					+ "      count_header\n" 
//					+ " FROM user_supply,\n" 
//					+ "      super_supply;\n"
					
//					+ "GRANT USAGE ON SEQUENCE\n" 
//					+ "      count_header_count_id_seq\n" 
//					+ "   TO user_finance;\n" 
//					+ "REVOKE USAGE ON SEQUENCE\n" 
//					+ "      count_header_count_id_seq\n" 
//					+ " FROM user_supply,\n" 
//					+ "      super_supply;\n" 
					
//					+ "INSERT INTO default_number (name, value)\n" 
//					+ "     VALUES ('PRINCIPAL', 488),\n" 
//					+ "            ('PURCHASE LEAD TIME', 4);\n" 

//					+ "UPDATE remittance_detail\n" 
//					+ "   SET payment = -payment\n" 
//					+ " WHERE     remit_id = 3540\n"
//					+ "       AND order_id = 12919\n"
//					+ "		  AND payment > 0;\n" 
					);
			ps.execute();
			ps.close();
			conn.commit();
			Database.error = "Update successful;" + msg;
		} catch (Exception e) {
			try {
				if (conn != null)
					conn.rollback();
				Database.error = "Update rollbacked:\n" + e.toString() + msg;
			} catch (Exception er) {
				Database.error = "Update rollbacked:\n" + er.toString() + msg;			}
			e.printStackTrace();
		} finally {
			try {
				if (ps != null)
					ps.close();
				conn.setAutoCommit(true);
				conn.close();
			} catch (Exception e) {
				Database.error = "Update rollbacked:\n" + e.toString() + msg;
			}
		}
    }
}
