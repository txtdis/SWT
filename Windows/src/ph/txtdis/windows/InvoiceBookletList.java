package ph.txtdis.windows;

import org.apache.commons.lang3.StringUtils;

public class InvoiceBookletList extends Report {

	public InvoiceBookletList(String string) {
		String searched;
		module = "Issued Invoice Booklet List";
		headers = new String[][] {
				{StringUtils.center("#", 3), "Line"},
				{StringUtils.center("START #", 9), "ID"},
				{StringUtils.center("END #", 9), "ID"},
				{StringUtils.center("SERIES", 6), "String"},
				{StringUtils.center("ISSUED TO", 30), "String"},
				{StringUtils.center("DATE", 10), "Date"}
		};
		if(StringUtils.isNumeric(string)) {
			searched = " AND (start_id <= " + string + "\n" +
					"	AND end_id >= " + string +  ")\n ";
		} else {
			searched = " AND name LIKE '%" + string.toUpperCase() + "%'\n";
		}

		data = new SQL().getDataArray("" +
				"SELECT ROW_NUMBER () OVER (ORDER BY start_id),\n" +
				"		  start_id,\n" +
				"         end_id,\n" +
				"         series,\n" +
				"         name,\n" +
				"         issue_date\n" +
				"    FROM invoice_booklet AS ib\n" +
				"         INNER JOIN contact_detail AS cd ON ib.rep_id = cd.id\n" +
				"   WHERE customer_id = 0\n" +
				searched +
				"ORDER BY start_id;\n" +
				"");
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		new InvoiceBookletList("");
		Database.getInstance().closeConnection();
	}
}
