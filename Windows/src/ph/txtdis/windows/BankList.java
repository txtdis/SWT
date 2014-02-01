package ph.txtdis.windows;

import org.apache.commons.lang3.StringUtils;

public class BankList extends Data {

	public BankList(String string) {
		type = Type.BANK_LIST;
		tableHeaders = new String[][] {
				{StringUtils.center("ID", 4), "ID"},
				{StringUtils.center("BANK NAME", 30), "String"},
				{StringUtils.center("ADDRESS", 40), "String"}
		};
		tableData = new Query().getTableData("" +
				"SELECT	cm.id, " +
				"		cm.name, " +
				"		((SELECT name FROM area WHERE id = a.district) || " +
				"			', ' || " +
				"		(SELECT name FROM area WHERE id = a.city)) AS address  " +
				"FROM 	customer_header AS cm " +
				"LEFT OUTER JOIN address AS a " +
				"	ON	cm.id = a.customer_id " +
				"WHERE 	cm.type_id = 10 " +
				"	AND cm.name LIKE '%" + string.toUpperCase() + "%'" +
				""
				);
	}
}
