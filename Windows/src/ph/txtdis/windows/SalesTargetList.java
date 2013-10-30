package ph.txtdis.windows;

import org.apache.commons.lang3.StringUtils;

public class SalesTargetList extends Report {

	public SalesTargetList() {
		module = "Target List";
		headers = new String[][] {
				{StringUtils.center("TYPE", 18), "String"},
				{StringUtils.center("ID", 4), "ID"},
				{StringUtils.center("CATEGORY", 4), "String"},
				{StringUtils.center("START", 10), "Date"},
				{StringUtils.center("END", 10), "Date"}
		};
		data = new Data().getDataArray("" +
				"SELECT	tt.name, " +
				"		th.target_id, " +
				"		if.name, " +
				"		th.start_date, " +
				"		th.end_date " +
				"FROM 	target_header AS th " +
				"INNER JOIN item_family AS if " +
				"	ON	th.category_id = if.id " +
				"INNER JOIN target_type AS tt " +
				"	ON	th.type_id = tt.id " +
				"ORDER BY th.target_id " 
				);
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin","localhost");
		SalesTargetList i = new SalesTargetList();
		for (Object[] os : i.getData()) {
			for (Object o : os) {
				System.out.print(o + ", ");
			}
			System.out.println();
		}
		Database.getInstance().closeConnection();
	}
}