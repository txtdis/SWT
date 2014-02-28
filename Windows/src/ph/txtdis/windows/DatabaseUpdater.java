package ph.txtdis.windows;

import java.sql.Connection;
import java.sql.SQLException;
import org.postgresql.util.PSQLException;


public class DatabaseUpdater extends Posting {

	@Override
	protected void postData() throws SQLException, PSQLException {
		updateVersion();

		ps = conn.prepareStatement(""
				// @sql:on
				+ "DELETE FROM count_adjustment "
				+ " WHERE reason LIKE $$%NOT COUNTED%$$"
				+ "GRANT INSERT, DELETE ON count_adjustment TO user_finance, user_supply;\n" 
				// @sql:off

//@sql:on
//				+ "ALTER TABLE invoice_header RENAME COLUMN invoice_id TO id;" 
//				+ "ALTER TABLE invoice_header RENAME COLUMN invoice_date TO post_date;" 
//				+ "ALTER TABLE invoice_detail RENAME COLUMN invoice_id TO id;" 
//				+ "ALTER TABLE sales_header RENAME COLUMN sales_id TO id;" 
//				+ "ALTER TABLE sales_header RENAME COLUMN sales_date TO post_date;" 
//				+ "ALTER TABLE sales_detail RENAME COLUMN sales_id TO id;" 
//				+ "ALTER TABLE sales_print_out RENAME COLUMN sales_id TO id;" 
//				+ "ALTER TABLE count_header RENAME COLUMN count_id TO id;" 
//				+ "ALTER TABLE count_header RENAME COLUMN count_date TO post_date;" 
//				+ "ALTER TABLE count_detail RENAME COLUMN count_id TO id;" 
//				+ "ALTER TABLE count_adjustment RENAME COLUMN count_date TO post_date;" 
//				+ "ALTER TABLE count_closure RENAME COLUMN count_date TO post_date;" 
//				+ "ALTER TABLE count_completion RENAME COLUMN count_date TO post_date;" 
//				+ "ALTER TABLE delivery_header RENAME COLUMN delivery_id TO id;" 
//				+ "ALTER TABLE delivery_header RENAME COLUMN delivery_date TO post_date;" 
//				+ "ALTER TABLE delivery_detail RENAME COLUMN delivery_id TO id;" 
//				+ "ALTER TABLE purchase_header RENAME COLUMN purchase_id TO id;" 
//				+ "ALTER TABLE purchase_header RENAME COLUMN purchase_date TO post_date;" 
//				+ "ALTER TABLE purchase_detail RENAME COLUMN purchase_id TO id;" 
//				+ "ALTER TABLE receiving_header RENAME COLUMN receiving_id TO id;" 
//				+ "ALTER TABLE receiving_header RENAME COLUMN receiving_date TO post_date;" 
//				+ "ALTER TABLE receiving_detail RENAME COLUMN receiving_id TO id;" 
//				+ "ALTER TABLE remit_header RENAME COLUMN remit_id TO id;" 
//				+ "ALTER TABLE remit_header RENAME COLUMN remit_date TO post_date;" 
//				+ "ALTER TABLE remit_cancellation RENAME COLUMN remit_id TO id;" 
//				+ "ALTER TABLE remit_detail RENAME COLUMN remit_id TO id;" 
				
//				+ "GRANT SELECT ON ALL TABLES IN SCHEMA public TO guest;\n"

//				+ "DELETE FROM delivery_header WHERE delivery_id = 664;\n" 

//				+ "UPDATE default_date\n" 
//				+ "   SET value = '2013-12-08'\n" 
//				+ " WHERE name = $$CLOSED-DSR-BEFORE-S/O CUTOFF$$;\n" 
//				+ "DELETE FROM delivery_header WHERE delivery_id = 303;\n" 
				
//				+ "DROP ROLE IF EXISTS	marivic;\n" 
//				+ "CREATE ROLE \"marivic\" LOGIN PASSWORD 'marvic' NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE;\n" 
//				+ "GRANT user_finance TO marivic;\n"
				
//				+ "GRANT INSERT\n" 
//				+ "   ON count_adjustment,\n" 
//				+ "      count_header\n" 
//				+ "   TO user_finance;\n" 
//				+ "REVOKE INSERT\n" 
//				+ "   ON count_adjustment,\n" 
//				+ "      count_header\n" 
//				+ " FROM user_supply,\n" 
//				+ "      super_supply;\n"
				
//				+ "GRANT USAGE ON SEQUENCE\n" 
//				+ "      count_header_count_id_seq\n" 
//				+ "   TO user_finance;\n" 
//				+ "REVOKE USAGE ON SEQUENCE\n" 
//				+ "      count_header_count_id_seq\n" 
//				+ " FROM user_supply,\n" 
//				+ "      super_supply;\n" 
				
//				+ "INSERT INTO default_number (name, value)\n" 
//				+ "     VALUES ('PRINCIPAL', 488),\n" 
//				+ "            ('PURCHASE LEAD TIME', 4);\n" 

//				+ "UPDATE remit_detail\n" 
//				+ "   SET payment = -payment\n" 
//				+ " WHERE     remit_id = 3540\n"
//				+ "       AND order_id = 12919\n"
//				+ "		  AND payment > 0;\n" 
//@sql:off
		        );
		ps.execute();
	}

	private void updateVersion() throws SQLException, PSQLException {
		ps = conn.prepareStatement("UPDATE version SET latest = ?;");
		ps.setString(1, Login.version());
		ps.executeUpdate();
	}

	private void executeDDL(String update) throws SQLException, PSQLException {
		executeDDL(update);
	}

	@SuppressWarnings("unused")
	private void renameColumn(String table, String from, String to) throws SQLException, PSQLException {
		executeDDL("ALTER TABLE " + table + " RENAME COLUMN " + from + " TO " + to + ";");
	}

	@SuppressWarnings("unused")
	private void dropColumn(String table, String column) throws SQLException, PSQLException {
		executeDDL("ALTER TABLE " + table + " DROP COLUMN " + column + ";");
	}

	@SuppressWarnings("unused")
	private void dropTable(String table, String from, String to) throws SQLException, PSQLException {
		executeDDL("DROP TABLE IF EXISTS " + table + " CASCADE;");
	}

	@SuppressWarnings("unused")
	private void dropUser(String user) throws SQLException, PSQLException {
		executeDDL("DROP ROLE IF EXISTS " + user + ";");
	}

	@Override
	protected Connection getConn() {
		DBMS.getInstance().closeConnection();
		try {
	        return DBMS.getInstance().getConn("postgres", "postgres", Login.server(), Login.network());
        } catch (SQLException e) {
	        e.printStackTrace();
	        return null;
        }
	}
}
