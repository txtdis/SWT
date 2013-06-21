package ph.txtdis.windows;

import org.apache.commons.lang3.StringUtils;

public class BankList extends Report {

	public BankList(String string) {
		module = "Bank List";
		headers = new String[][] {
				{StringUtils.center("ID", 4), "Integer"},
				{StringUtils.center("BANK NAME", 30), "String"},
				{StringUtils.center("ADDRESS", 40), "String"}
		};
		data = new SQL().getDataArray("" +
				"SELECT	cm.id, " +
				"		cm.name, " +
				"		((SELECT name FROM area WHERE id = a.district) || " +
				"			', ' || " +
				"		(SELECT name FROM area WHERE id = a.city)) AS address  " +
				"FROM 	customer_master AS cm " +
				"LEFT OUTER JOIN address AS a " +
				"	ON	cm.id = a.customer_id " +
				"WHERE 	cm.type_id = 10 " +
				"	AND cm.name LIKE '%" + string.toUpperCase() + "%'" +
				""
				);
	}
}
